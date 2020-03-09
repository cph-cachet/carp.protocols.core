package dk.cachet.carp.studies.application

import dk.cachet.carp.common.EmailAddress
import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.common.users.EmailAccountIdentity
import dk.cachet.carp.deployment.application.DeploymentService
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.protocols.domain.InvalidConfigurationError
import dk.cachet.carp.protocols.domain.StudyProtocolSnapshot
import dk.cachet.carp.studies.domain.Study
import dk.cachet.carp.studies.domain.StudyRepository
import dk.cachet.carp.studies.domain.StudyStatus
import dk.cachet.carp.studies.domain.users.AssignParticipantDevices
import dk.cachet.carp.studies.domain.users.DeanonymizedParticipation
import dk.cachet.carp.studies.domain.users.deviceRoles
import dk.cachet.carp.studies.domain.users.Participant
import dk.cachet.carp.studies.domain.users.participantIds
import dk.cachet.carp.studies.domain.users.StudyOwner


/**
 * Implementation of [StudyService] which allows creating and managing studies.
 */
class StudyServiceHost(
    private val repository: StudyRepository,
    private val deploymentService: DeploymentService
) : StudyService
{
    /**
     * Create a new study for the specified [owner].
     *
     * @param name A descriptive name for the study, assigned by, and only visible to, the [owner].
     * @param invitation
     *  An optional description of the study, shared with participants once they are invited.
     *  In case no description is specified, [name] is used as the name in [invitation].
     */
    override suspend fun createStudy( owner: StudyOwner, name: String, invitation: StudyInvitation? ): StudyStatus
    {
        val ensuredInvitation = invitation ?: StudyInvitation( name )
        val study = Study( owner, name, ensuredInvitation )

        repository.add( study )

        return study.getStatus()
    }

    /**
     * Get the status for a study with the given [studyId].
     *
     * @param studyId The id of the study to return [StudyStatus] for.
     *
     * @throws IllegalArgumentException when a deployment with [studyId] does not exist.
     */
    override suspend fun getStudyStatus( studyId: UUID ): StudyStatus
    {
        val study = repository.getById( studyId )
        require( study != null )

        return study.getStatus()
    }

    /**
     * Get status for all studies created by the specified [owner].
     */
    override suspend fun getStudiesOverview( owner: StudyOwner ): List<StudyStatus> =
        repository.getForOwner( owner ).map { it.getStatus() }

    /**
     * Add a [Participant] to the study with the specified [studyId], identified by the specified [email] address.
     * In case the [email] was already added before, the same [Participant] is returned.
     *
     * @throws IllegalArgumentException when a study with [studyId] does not exist.
     */
    override suspend fun addParticipant( studyId: UUID, email: EmailAddress ): Participant
    {
        // Verify whether participant was already added.
        val identity = EmailAccountIdentity( email )
        var participant = repository.participants.getAll( studyId ).firstOrNull { it.accountIdentity == identity }

        // Add new participant in case it was not added before.
        if ( participant == null )
        {
            participant = Participant( identity )
            repository.participants.addRemove( studyId, setOf(participant), setOf() )
        }

        return participant
    }

    /**
     * Get all [Participant]s for the study with the specified [studyId].
     *
     * @throws IllegalArgumentException when a study with [studyId] does not exist.
     */
    override suspend fun getParticipants( studyId: UUID ): List<Participant> =
        repository.participants.getAll( studyId )

    /**
     * Specify the study [protocol] to use for the study with the specified [studyId].
     *
     * @throws IllegalArgumentException when a study with [studyId] does not exist,
     * when the provided [protocol] snapshot is invalid,
     * or when the protocol contains errors preventing it from being used in deployments.
     */
    override suspend fun setProtocol( studyId: UUID, protocol: StudyProtocolSnapshot ): StudyStatus
    {
        val study: Study? = repository.getById( studyId )
        require( study != null )

        // Configure study to use the protocol.
        try { study.protocolSnapshot = protocol }
        catch ( e: InvalidConfigurationError ) { throw IllegalArgumentException( e.message ) }

        repository.updateProtocol( study.id, protocol )

        return study.getStatus()
    }

    /**
     * Lock in the current study protocol so that the study may be deployed to participants.
     *
     * @throws IllegalArgumentException when a study with [studyId] does not exist.
     * @throws IllegalStateException when no study protocol for the given study is set yet.
     */
    override suspend fun goLive( studyId: UUID ): StudyStatus
    {
        val study: Study? = repository.getById( studyId )
        require( study != null )

        study.goLive()
        repository.updateLiveStatus( study.id, study.isLive )

        return study.getStatus()
    }

    /**
     * Deploy the study with the given [studyId] to a [group] of previously added participants.
     *
     * @throws IllegalArgumentException when:
     *  - a study with [studyId] does not exist
     *  - [group] is empty
     *  - any of the participants specified in [group] does not exist
     *  - any of the device roles specified in [group] are not part of the configured study protocol
     *  - not all devices part of the study have been assigned a participant
     * @throws IllegalStateException when the study is not yet ready for deployment.
     */
    override suspend fun deployParticipantGroup( studyId: UUID, group: Set<AssignParticipantDevices> ): StudyStatus
    {
        require( group.isNotEmpty() ) { "No participants to deploy specified." }

        // Verify whether the study is ready for deployment.
        val study: Study? = repository.getById( studyId )
        require( study != null ) { "Study with the specified studyId is not found." }
        check( study.canDeployToParticipants ) { "Study is not yet ready to be deployed to participants." }

        // Verify whether the master device roles to deploy exist in the protocol.
        val masterDevices = study.protocolSnapshot!!.masterDevices.map { it.roleName }.toSet()
        require( group.deviceRoles().all { masterDevices.contains( it ) } )
            { "One of the specified device roles is not part of the configured study protocol." }

        // Verify whether all master devices in the study protocol have been assigned to a participant.
        require( group.deviceRoles().containsAll( masterDevices ) )
            { "Not all devices required for this study have been assigned to a participant." }

        // Get participant information.
        val allParticipants = repository.participants.getAll( studyId ).associateBy { it.id }
        require( group.participantIds().all { allParticipants.contains( it ) } )
            { "One of the specified participants is not part of this study." }

        // Create deployment and add participations to study.
        // TODO: How to deal with failing or partially succeeding requests?
        //       In a distributed setup, deploymentService would be network calls.
        val deploymentStatus = deploymentService.createStudyDeployment( study.protocolSnapshot!! )
        for ( toAssign in group )
        {
            val identity: AccountIdentity = allParticipants.getValue( toAssign.participantId ).accountIdentity
            val participation = deploymentService.addParticipation(
                deploymentStatus.studyDeploymentId,
                toAssign.deviceRoleNames,
                identity,
                study.invitation )

            study.addParticipation( DeanonymizedParticipation( toAssign.participantId, participation ) )
        }

        repository.participations.addRemove( study.id, study.participations, setOf() )

        return study.getStatus()
    }
}
