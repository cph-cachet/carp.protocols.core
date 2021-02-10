package dk.cachet.carp.deployment.domain.users

import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.domain.createActiveDeployment
import kotlin.test.*


/**
 * Tests for [ActiveParticipationInvitation].
 */
class ActiveParticipationInvitationTest
{
    @Test
    fun filterActiveParticipationInvitations_only_returns_active_deployments()
    {
        val deviceRole = "Participant's phone"
        val activeGroup = ParticipantGroup.fromDeployment( createActiveDeployment( deviceRole ) )
        val stoppedGroup = ParticipantGroup.fromDeployment( createActiveDeployment( deviceRole ) )
        stoppedGroup.studyDeploymentStopped()

        val participation = Participation( activeGroup.studyDeploymentId )
        val invitation = ParticipationInvitation( participation, StudyInvitation.empty(), setOf( deviceRole ) )

        val activeInvitations = filterActiveParticipationInvitations(
            setOf( invitation ),
            listOf( activeGroup, stoppedGroup )
        )

        assertEquals( participation, activeInvitations.single().participation )
    }

    @Test
    fun filterActiveParticipationInvitations_includes_device_registration()
    {
        val deviceRole = "Participant's phone"
        val deployment = createActiveDeployment( deviceRole )
        val group = ParticipantGroup.fromDeployment( deployment )

        val participation = Participation( group.studyDeploymentId )
        val invitation = ParticipationInvitation( participation, StudyInvitation.empty(), setOf( deviceRole ) )

        // When the device is not registered in the deployment, this is communicated in the active invitation.
        var activeInvitation = filterActiveParticipationInvitations(
            setOf( invitation ),
            listOf( group )
        ).first()
        var retrievedRegistration = activeInvitation.assignedDevices.first { it.device.roleName == deviceRole }.registration
        assertNull( retrievedRegistration )

        // Once the device is registered, this is communicated in the active invitation.
        val toRegister = group.assignedMasterDevices.first { it.device.roleName == deviceRole }.device
        val deviceRegistration = toRegister.createRegistration()
        group.updateDeviceRegistration( toRegister, deviceRegistration )
        activeInvitation = filterActiveParticipationInvitations(
            setOf( invitation ),
            listOf( group )
        ).first()
        retrievedRegistration = activeInvitation.assignedDevices.first { it.device.roleName == deviceRole }.registration
        assertEquals( deviceRegistration, retrievedRegistration )
    }

    @Test
    fun filterActiveParticipationInvitations_fails_when_required_deployment_is_not_passed()
    {
        val unknownDeployment = UUID.randomUUID()
        val participation = Participation( unknownDeployment )
        val invitation = ParticipationInvitation( participation, StudyInvitation.empty(), setOf( "Smartphone" ) )

        assertFailsWith<IllegalArgumentException>
        {
            filterActiveParticipationInvitations( setOf( invitation ), emptyList() )
        }
    }

    @Test
    fun filterActiveParticipationInvitations_fails_when_participation_device_role_does_not_match()
    {
        val group = ParticipantGroup.fromDeployment( createActiveDeployment( "Master" ) )

        val participation = Participation( group.studyDeploymentId )
        val invitation = ParticipationInvitation( participation, StudyInvitation.empty(), setOf( "Incorrect device role" ) )

        assertFailsWith<IllegalArgumentException>
        {
            filterActiveParticipationInvitations( setOf( invitation ), listOf( group ) )
        }
    }
}
