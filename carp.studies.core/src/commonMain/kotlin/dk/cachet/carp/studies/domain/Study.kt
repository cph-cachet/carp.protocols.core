package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.UUID


/**
 * Represents a study which can be pilot tested and eventually 'go live', for which a recruitment goal can be set, and participants can be recruited.
 */
class Study(
    /**
     * The person or group that created this [Study].
     */
    val owner: StudyOwner,
    /**
     * A descriptive name for the study, assigned by, and only visible to, the [StudyOwner].
     */
    val name: String,
    /**
     * A description of the study, visible to all participants.
     */
    val description: StudyDescription = StudyDescription.empty(),
    val id: UUID = UUID.randomUUID()
)
{
    companion object Factory
    {
        fun fromSnapshot( snapshot: StudySnapshot ): Study
        {
            val study = Study( StudyOwner( snapshot.ownerId ), snapshot.name, snapshot.description, snapshot.studyId )

            // Add participants.
            snapshot.participantIds.forEach { study.includeParticipant( it ) }

            return study
        }
    }


    private val _participantIds: MutableSet<UUID> = mutableSetOf()

    /**
     * The set of participants which have been included in this [Study].
     */
    val participantIds: Set<UUID>
        get() = _participantIds

    /**
     * Get the status (serializable) of this [Study].
     */
    fun getStatus(): StudyStatus = StudyStatus( id, name )

    /**
     * Include a participant in this [Study].
     */
    fun includeParticipant( participantId: UUID ) = _participantIds.add( participantId )


    /**
     * Get a serializable snapshot of the current state of this [Study].
     */
    fun getSnapshot(): StudySnapshot
    {
        return StudySnapshot.fromStudy( this )
    }
}
