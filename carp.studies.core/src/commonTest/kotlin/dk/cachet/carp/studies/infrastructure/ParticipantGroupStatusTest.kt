package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.deployments.application.StudyDeploymentStatus
import dk.cachet.carp.studies.application.users.Participant
import dk.cachet.carp.studies.application.users.ParticipantGroupStatus
import kotlin.test.*


/**
 * Tests for [ParticipantGroupStatus] relying on core infrastructure.
 */
class ParticipantGroupStatusTest
{
    @Test
    fun can_serialize_and_deserialize_ParticipantGroupStatus_using_JSON()
    {
        val studyDeploymentId = UUID.randomUUID()
        val deploymentStatus = StudyDeploymentStatus.Invited( studyDeploymentId, listOf(), null )
        val participants = setOf( Participant( AccountIdentity.fromEmailAddress( "test@test.com" ) ) )
        val groupStatus = ParticipantGroupStatus( deploymentStatus, participants )

        val serialized: String = JSON.encodeToString( ParticipantGroupStatus.serializer(), groupStatus )
        val parsed: ParticipantGroupStatus = JSON.decodeFromString( ParticipantGroupStatus.serializer(), serialized )

        assertEquals( groupStatus, parsed )
    }
}
