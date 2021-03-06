package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.application.DateTime
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.domain.Snapshot
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import kotlinx.serialization.Serializable


@Serializable
data class StudySnapshot(
    val studyId: UUID,
    val ownerId: UUID,
    val name: String,
    val description: String,
    val invitation: StudyInvitation,
    override val creationDate: DateTime,
    val protocolSnapshot: StudyProtocolSnapshot?,
    val isLive: Boolean
) : Snapshot<Study>
{
    companion object
    {
        /**
         * Create a snapshot of the specified [Study].
         *
         * @param study The [Study] to create a snapshot for.
         */
        fun fromStudy( study: Study ): StudySnapshot
        {
            return StudySnapshot(
                studyId = study.id,
                ownerId = study.owner.id,
                name = study.name,
                description = study.description,
                invitation = study.invitation,
                creationDate = study.creationDate,
                protocolSnapshot = study.protocolSnapshot,
                isLive = study.isLive )
        }
    }

    override fun toObject(): Study = Study.fromSnapshot( this )
}
