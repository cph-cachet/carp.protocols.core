package dk.cachet.carp.clients

import dk.cachet.carp.clients.domain.SmartphoneClient
import dk.cachet.carp.clients.domain.StudyRuntimeStatus
import dk.cachet.carp.clients.domain.createDataCollectorFactory
import dk.cachet.carp.clients.domain.createParticipantInvitation
import dk.cachet.carp.clients.domain.data.DataListener
import dk.cachet.carp.clients.infrastructure.InMemoryClientRepository
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.CarpDataTypes
import dk.cachet.carp.common.application.devices.Smartphone
import dk.cachet.carp.common.application.services.createApplicationServiceAdapter
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.common.domain.users.Account
import dk.cachet.carp.common.infrastructure.services.SingleThreadedEventBus
import dk.cachet.carp.common.infrastructure.test.StubDeviceDescriptor
import dk.cachet.carp.deployments.application.DeploymentService
import dk.cachet.carp.deployments.application.DeploymentServiceHost
import dk.cachet.carp.deployments.application.ParticipationService
import dk.cachet.carp.deployments.application.ParticipationServiceHost
import dk.cachet.carp.deployments.application.users.ActiveParticipationInvitation
import dk.cachet.carp.deployments.domain.users.ParticipantGroupService
import dk.cachet.carp.deployments.infrastructure.InMemoryAccountService
import dk.cachet.carp.deployments.infrastructure.InMemoryDeploymentRepository
import dk.cachet.carp.deployments.infrastructure.InMemoryParticipationRepository
import dk.cachet.carp.protocols.domain.ProtocolOwner
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.domain.start
import dk.cachet.carp.test.runSuspendTest
import kotlin.test.*


class ClientCodeSamples
{
    @Test
    @Suppress( "UnusedPrivateMember", "UNUSED_VARIABLE" )
    fun readme() = runSuspendTest {
        val (participationService, deploymentService) = createEndpoints()
        val dataCollectorFactory = createDataCollectorFactory()

        // Retrieve invitation to participate in the study using a specific device.
        val account: Account = getLoggedInUser()
        val invitation: ActiveParticipationInvitation =
            participationService.getActiveParticipationInvitations( account.id ).first()
        val studyDeploymentId: UUID = invitation.participation.studyDeploymentId
        val deviceToUse: String = invitation.assignedDevices.first().device.roleName // This matches "Patient's phone".

        // Create a study runtime for the study.
        val clientRepository = createRepository()
        val client = SmartphoneClient( clientRepository, deploymentService, dataCollectorFactory )
        client.configure {
            // Device-specific registration options can be accessed from here.
            // Depending on the device type, different options are available.
            // E.g., for a smartphone, a UUID deviceId is generated. To override this default:
            deviceId = "xxxxxxxxx"
        }
        var status: StudyRuntimeStatus = client.addStudy( studyDeploymentId, deviceToUse )

        // Register connected devices in case needed.
        if ( status is StudyRuntimeStatus.RegisteringDevices )
        {
            val connectedDevice = status.remainingDevicesToRegister.first()
            val connectedRegistration = connectedDevice.createRegistration()
            deploymentService.registerDevice( studyDeploymentId, connectedDevice.roleName, connectedRegistration )

            // Re-try deployment now that devices have been registered.
            status = client.tryDeployment( status.id )
            val isDeployed = status is StudyRuntimeStatus.Deployed // True.
        }
    }


    private suspend fun createEndpoints(): Pair<ParticipationService, DeploymentService>
    {
        val eventBus = SingleThreadedEventBus()

        val deploymentService = DeploymentServiceHost(
            InMemoryDeploymentRepository(),
            eventBus.createApplicationServiceAdapter( DeploymentService::class )
        )

        val participationService = ParticipationServiceHost(
            InMemoryParticipationRepository(),
            ParticipantGroupService( accountService ),
            eventBus.createApplicationServiceAdapter( ParticipationService::class )
        )

        // Create deployment for the example protocol.
        val protocol = createExampleProtocol()
        val invitation = createParticipantInvitation( protocol, accountIdentity )
        deploymentService.createStudyDeployment( UUID.randomUUID(), protocol.getSnapshot(), listOf( invitation ) )

        return Pair( participationService, deploymentService )
    }

    private fun createRepository() = InMemoryClientRepository()

    /**
     * This is the protocol created in ProtocolsCodeSamples.readme().
     */
    private fun createExampleProtocol(): StudyProtocol
    {
        val owner = ProtocolOwner()
        val protocol = StudyProtocol( owner, "Track patient movement" )

        val phone = Smartphone( "Patient's phone" )
        protocol.addMasterDevice( phone )

        // This is not in the protocols readme, but is needed for the connected device example.
        val connected = StubDeviceDescriptor( "External sensor" )
        protocol.addConnectedDevice( connected, phone )

        val sensors = Smartphone.Sensors
        val trackMovement = Smartphone.Tasks.BACKGROUND.create( "Track movement" ) {
            measures = listOf( sensors.GEOLOCATION.measure(), sensors.STEP_COUNT.measure() )
        }
        protocol.addTaskControl( phone.atStartOfStudy().start( trackMovement, phone ) )


        return protocol
    }

    /**
     * A stub [DataListener] which supports the expected data types in [createExampleProtocol].
     */
    private fun createDataCollectorFactory() = createDataCollectorFactory(
        CarpDataTypes.GEOLOCATION, CarpDataTypes.STEP_COUNT
    )

    private val accountService = InMemoryAccountService()
    private val accountIdentity: AccountIdentity = AccountIdentity.fromUsername( "Test user" )
    private suspend fun getLoggedInUser(): Account = accountService.findAccount( accountIdentity )!!
}
