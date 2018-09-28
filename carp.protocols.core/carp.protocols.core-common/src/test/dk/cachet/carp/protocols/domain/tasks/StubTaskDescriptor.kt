package dk.cachet.carp.protocols.domain.tasks

import dk.cachet.carp.protocols.domain.serialization.*
import dk.cachet.carp.protocols.domain.tasks.measures.Measure
import kotlinx.serialization.Serializable


@Serializable
data class StubTaskDescriptor(
    override val name: String = "Stub task",
    // TODO: Use the following serializer in JVM.
    //@Serializable( MeasuresSerializer::class )
    @Serializable( PolymorphicArrayListSerializer::class )
    override val measures: List<Measure> = listOf() ) : TaskDescriptor()
{
    companion object
    {
        init { PolymorphicSerializer.registerSerializer( StubTaskDescriptor::class, "dk.cachet.carp.protocols.domain.tasks.StubTaskDescriptor" ) }
    }
}