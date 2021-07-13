package dk.cachet.carp.deployments

import dk.cachet.carp.common.application.DateTime
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.Smartphone
import dk.cachet.carp.common.application.services.EventBus
import dk.cachet.carp.common.application.services.createApplicationServiceAdapter
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.common.infrastructure.services.SingleThreadedEventBus
import dk.cachet.carp.deployments.application.DeploymentService
import dk.cachet.carp.deployments.application.DeploymentServiceHost
import dk.cachet.carp.deployments.application.DeviceDeploymentStatus
import dk.cachet.carp.deployments.application.MasterDeviceDeployment
import dk.cachet.carp.deployments.application.StudyDeploymentStatus
import dk.cachet.carp.deployments.application.users.ParticipantInvitation
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.deployments.infrastructure.InMemoryDeploymentRepository
import dk.cachet.carp.protocols.domain.ProtocolOwner
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.domain.start
import dk.cachet.carp.test.runSuspendTest
import kotlin.test.*


class DeploymentCodeSamples
{
    @Test
    @Suppress( "UnusedPrivateMember", "UNUSED_VARIABLE" )
    fun readme() = runSuspendTest {
        val deploymentService: DeploymentService = createDeploymentEndpoint()
        val trackPatientStudy: StudyProtocol = createExampleProtocol()
        val patientPhone: Smartphone = trackPatientStudy.masterDevices.first() as Smartphone // "Patient's phone"

        // This is called by `StudyService` when deploying a participant group.
        val invitation = ParticipantInvitation(
            participantId = UUID.randomUUID(),
            assignedMasterDeviceRoleNames = setOf( patientPhone.roleName ),
            identity = AccountIdentity.fromEmailAddress( "test@test.com" ),
            invitation = StudyInvitation( "Movement study", "This study tracks your movements." )
        )
        val studyDeploymentId = UUID.randomUUID()
        deploymentService.createStudyDeployment(
            studyDeploymentId,
            trackPatientStudy.getSnapshot(),
            listOf( invitation )
        )

        // What comes after is similar to what is called by the client in `carp.client`:
        // - Register the device to be deployed.
        val registration = patientPhone.createRegistration()
        var status = deploymentService.registerDevice( studyDeploymentId, patientPhone.roleName, registration )

        // - Retrieve information on what to run and indicate the device is ready to collect the requested data.
        val patientPhoneStatus: DeviceDeploymentStatus = status.getDeviceStatus( patientPhone )
        if ( patientPhoneStatus.canObtainDeviceDeployment ) // True since there are no dependent devices.
        {
            val deploymentInformation: MasterDeviceDeployment =
                deploymentService.getDeviceDeploymentFor( studyDeploymentId, patientPhone.roleName )
            val deploymentDate: DateTime = deploymentInformation.lastUpdateDate // To verify correct deployment.
            deploymentService.deploymentSuccessful( studyDeploymentId, patientPhone.roleName, deploymentDate )
        }

        // Now that all devices have been registered and deployed, the deployment is ready.
        status = deploymentService.getStudyDeploymentStatus( studyDeploymentId )
        val isReady = status is StudyDeploymentStatus.DeploymentReady // True.
    }


    /**
     * This is the protocol created in ProtocolsCodeSamples.readme().
     */
    private fun createExampleProtocol(): StudyProtocol
    {
        val owner = ProtocolOwner()
        val protocol = StudyProtocol( owner, "Track patient movement" )

        val phone = Smartphone( "Patient's phone" )
        protocol.addMasterDevice( phone )

        val sensors = Smartphone.Sensors
        val trackMovement = Smartphone.Tasks.BACKGROUND.create( "Track movement" ) {
            measures = listOf( sensors.GEOLOCATION.measure(), sensors.STEP_COUNT.measure() )
        }
        protocol.addTaskControl( phone.atStartOfStudy().start( trackMovement, phone ) )

        return protocol
    }

    private val eventBus: EventBus = SingleThreadedEventBus()
    private fun createDeploymentEndpoint(): DeploymentService = DeploymentServiceHost(
        InMemoryDeploymentRepository(),
        eventBus.createApplicationServiceAdapter( DeploymentService::class ) )
}
