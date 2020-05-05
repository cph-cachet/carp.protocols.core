package dk.cachet.carp.client

import dk.cachet.carp.client.domain.SmartphoneManager
import dk.cachet.carp.client.domain.StudyRuntime
import dk.cachet.carp.client.domain.createSmartphoneManager
import dk.cachet.carp.common.UUID
import dk.cachet.carp.common.users.Account
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.deployment.application.DeploymentService
import dk.cachet.carp.deployment.application.DeploymentServiceHost
import dk.cachet.carp.deployment.domain.users.ParticipationInvitation
import dk.cachet.carp.deployment.domain.users.StudyInvitation
import dk.cachet.carp.deployment.infrastructure.InMemoryAccountService
import dk.cachet.carp.deployment.infrastructure.InMemoryDeploymentRepository
import dk.cachet.carp.protocols.domain.ProtocolOwner
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.domain.devices.Smartphone
import dk.cachet.carp.protocols.domain.tasks.ConcurrentTask
import dk.cachet.carp.test.runBlockingTest
import kotlin.test.*


class ClientCodeSamples
{
    @Test
    fun readme() = runBlockingTest {
        val deploymentService = createDeploymentEndpoint()

        // Retrieve invitation to participate in the study using a specific device.
        val account: Account = getLoggedInUser()
        val invitation: ParticipationInvitation = deploymentService.getParticipationInvitations( account.id ).first()
        val studyDeploymentId: UUID = invitation.participation.studyDeploymentId
        val deviceToUse: String = invitation.deviceRoleNames.first() // This matches "Patient's phone".

        // Create a study runtime for the study.
        val clientManager: SmartphoneManager = createSmartphoneManager( deploymentService )
        val runtime: StudyRuntime = clientManager.addStudy( studyDeploymentId, deviceToUse )
        var isDeployed = runtime.isDeployed // True, because there are no dependent devices.

        // Suppose a deployment also depends on a "Clinician's phone" to be registered; deployment cannot complete yet.
        // After the clinician's phone has been registered, attempt deployment again.
        isDeployed = runtime.tryDeployment() // True once dependent clients have been registered.
    }


    private suspend fun createDeploymentEndpoint(): DeploymentService
    {
        val service = DeploymentServiceHost( InMemoryDeploymentRepository(), accountService )

        // Create deployment for the example protocol.
        val protocol = createExampleProtocol()
        val status = service.createStudyDeployment( protocol.getSnapshot() )

        // Invite a participant.
        val phone = protocol.masterDevices.first()
        val invitation = StudyInvitation.empty()
        service.addParticipation( status.studyDeploymentId, setOf( phone.roleName ), accountIdentity, invitation )
        val account = accountService.findAccount( accountIdentity )

        return service
    }

    /**
     * This is the protocol created in ProtocolsCodeSamples.readme().
     */
    private fun createExampleProtocol(): StudyProtocol
    {
        val owner = ProtocolOwner()
        val protocol = StudyProtocol( owner, "Example study" )

        val phone = Smartphone( "Patient's phone" )
        protocol.addMasterDevice( phone )

        val measures = listOf( Smartphone.Sensors.geolocation(), Smartphone.Sensors.stepcount() )
        val startMeasures = ConcurrentTask( "Start measures", measures )
        protocol.addTriggeredTask( phone.atStartOfStudy(), startMeasures, phone )

        return protocol
    }

    private val accountService = InMemoryAccountService()
    private val accountIdentity: AccountIdentity = AccountIdentity.fromUsername( "Test user" )
    private suspend fun getLoggedInUser(): Account = accountService.findAccount( accountIdentity )!!
}