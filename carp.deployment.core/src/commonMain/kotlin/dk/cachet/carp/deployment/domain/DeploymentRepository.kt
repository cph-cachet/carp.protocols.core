package dk.cachet.carp.deployment.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.repository.RepositorySubCollection
import dk.cachet.carp.deployment.domain.users.AccountParticipation
import dk.cachet.carp.deployment.domain.users.ParticipationInvitation
import dk.cachet.carp.protocols.domain.devices.AnyDeviceDescriptor
import dk.cachet.carp.protocols.domain.devices.DeviceRegistration


interface DeploymentRepository
{
    val invitations: RepositorySubCollection<UUID, ParticipationInvitation>
    val participations: RepositorySubCollection<UUID, AccountParticipation>

    /**
     * Adds the specified [studyDeployment] to the repository.
     *
     * @throws IllegalArgumentException when a study deployment with the same id already exists.
     */
    fun add( studyDeployment: StudyDeployment )

    /**
     * Return the [StudyDeployment] with the specified [id], or null when no study deployment is found.
     *
     * @param id The id of the [StudyDeployment] to search for.
     */
    fun getStudyDeploymentBy( id: UUID ): StudyDeployment?

    /**
     * Update a [studyDeployment] which is already stored in this repository.
     *
     * @param studyDeployment The updated version of the study deployment to store.
     * @throws IllegalArgumentException when no previous version of this study deployment is stored in the repository.
     */
    fun update( studyDeployment: StudyDeployment )

    /**
     * Register device with descriptor [descriptor] and registration [registration]
     * on study deployment with id [studyDeploymentId]
     */
    fun registerDevice( studyDeploymentId: UUID, descriptor: AnyDeviceDescriptor, registration: DeviceRegistration )
}
