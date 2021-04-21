package dk.cachet.carp.deployments.domain.users

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.deployments.application.users.Participation
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.deployments.domain.createActiveDeployment
import kotlin.test.*


/**
 * Tests for [AccountParticipation].
 */
class AccountParticipationTest
{
    @Test
    fun filterActiveParticipationInvitations_only_returns_active_deployments()
    {
        val deviceRole = "Participant's phone"
        val activeGroup = ParticipantGroup.fromNewDeployment( createActiveDeployment( deviceRole ) )
        val stoppedGroup = ParticipantGroup.fromNewDeployment( createActiveDeployment( deviceRole ) )
        stoppedGroup.studyDeploymentStopped()

        val participation = Participation( activeGroup.studyDeploymentId )
        val accountId = UUID.randomUUID()
        val invitation = AccountParticipation( accountId, participation, StudyInvitation.empty(), setOf( deviceRole ) )

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
        val group = ParticipantGroup.fromNewDeployment( deployment )

        val participation = Participation( group.studyDeploymentId )
        val accountId = UUID.randomUUID()
        val invitation = AccountParticipation( accountId, participation, StudyInvitation.empty(), setOf( deviceRole ) )

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
        val accountId = UUID.randomUUID()
        val invitation = AccountParticipation( accountId, participation, StudyInvitation.empty(), setOf( "Smartphone" ) )

        assertFailsWith<IllegalArgumentException>
        {
            filterActiveParticipationInvitations( setOf( invitation ), emptyList() )
        }
    }

    @Test
    fun filterActiveParticipationInvitations_fails_when_participation_device_role_does_not_match()
    {
        val group = ParticipantGroup.fromNewDeployment( createActiveDeployment( "Master" ) )

        val participation = Participation( group.studyDeploymentId )
        val accountId = UUID.randomUUID()
        val invitation = AccountParticipation( accountId, participation, StudyInvitation.empty(), setOf( "Incorrect device role" ) )

        assertFailsWith<IllegalArgumentException>
        {
            filterActiveParticipationInvitations( setOf( invitation ), listOf( group ) )
        }
    }
}