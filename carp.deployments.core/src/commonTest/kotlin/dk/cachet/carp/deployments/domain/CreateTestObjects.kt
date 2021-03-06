package dk.cachet.carp.deployments.domain

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.input.CarpInputDataTypes
import dk.cachet.carp.common.application.data.input.CustomInput
import dk.cachet.carp.common.application.data.input.Sex
import dk.cachet.carp.common.application.data.input.elements.Text
import dk.cachet.carp.common.application.devices.AnyMasterDeviceDescriptor
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.common.application.users.ParticipantAttribute
import dk.cachet.carp.common.domain.users.Account
import dk.cachet.carp.deployments.application.users.ParticipantInvitation
import dk.cachet.carp.deployments.application.users.Participation
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.deployments.domain.users.ParticipantGroup
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.infrastructure.test.createSingleMasterDeviceProtocol
import dk.cachet.carp.protocols.infrastructure.test.createSingleMasterWithConnectedDeviceProtocol


fun studyDeploymentFor( protocol: StudyProtocol ): StudyDeployment
{
    val snapshot = protocol.getSnapshot()
    return StudyDeployment( snapshot )
}

/**
 * Creates a study deployment with a registered device and participation added.
 */
fun createComplexDeployment(): StudyDeployment
{
    val protocol = createSingleMasterWithConnectedDeviceProtocol( "Master", "Connected" )
    val deployment = studyDeploymentFor( protocol )

    // Add device registrations.
    val master = deployment.registrableDevices.first { it.device.roleName == "Master" }.device as AnyMasterDeviceDescriptor
    val connected = deployment.registrableDevices.first { it.device.roleName == "Connected" }.device
    deployment.registerDevice( master, master.createRegistration() )
    deployment.registerDevice( connected, connected.createRegistration() )

    // Deploy a device.
    val deviceDeployment = deployment.getDeviceDeploymentFor( master )
    deployment.deviceDeployed( master, deviceDeployment.lastUpdateDate )

    deployment.stop()

    // Remove events since tests building on top of this are not interested in how this object was constructed.
    deployment.consumeEvents()

    return deployment
}

/**
 * Creates a study deployment that is active (not stopped) for a study protocol with a single master device.
 */
fun createActiveDeployment( masterDeviceRoleName: String ): StudyDeployment
{
    val protocol = createSingleMasterDeviceProtocol( masterDeviceRoleName )

    return studyDeploymentFor( protocol )
}

/**
 * Creates a stopped study deployment for a study protocol with a single master device.
 */
fun createStoppedDeployment( masterDeviceRoleName: String ): StudyDeployment =
    createActiveDeployment( masterDeviceRoleName ).apply { stop() }

/**
 * Create a participant invitation for a specific [identity], or newly created identity when null,
 * which is assigned all devices in [protocol].
 */
fun createParticipantInvitation( protocol: StudyProtocol, identity: AccountIdentity? = null ): ParticipantInvitation =
    ParticipantInvitation(
        UUID.randomUUID(),
        protocol.masterDevices.map { it.roleName }.toSet(),
        identity ?: AccountIdentity.fromUsername( "Test" ),
        StudyInvitation.empty()
    )

/**
 * Creates a participant group with a default and custom expected participant attribute and sets the data.
 */
fun createComplexParticipantGroup(): ParticipantGroup
{
    val protocol: StudyProtocol = createSingleMasterDeviceProtocol()
    val defaultAttribute = ParticipantAttribute.DefaultParticipantAttribute( CarpInputDataTypes.SEX )
    protocol.addExpectedParticipantData( defaultAttribute )
    val customAttribute = ParticipantAttribute.CustomParticipantAttribute( Text( "Name" ) )
    protocol.addExpectedParticipantData( customAttribute )
    val deployment = StudyDeployment( protocol.getSnapshot() )

    return ParticipantGroup.fromNewDeployment( deployment ).apply {
        addParticipation(
            Account.withEmailIdentity( "test@test.com" ),
            StudyInvitation.empty(),
            Participation( studyDeploymentId ),
            setOf( protocol.masterDevices.first() )
        )
        setData( CarpInputDataTypes, CarpInputDataTypes.SEX, Sex.Male )
        setData( CarpInputDataTypes, customAttribute.inputType, CustomInput( "Steven" ) )
        studyDeploymentStopped()
    }
}
