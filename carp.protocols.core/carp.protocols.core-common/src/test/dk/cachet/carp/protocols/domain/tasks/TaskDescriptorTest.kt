package dk.cachet.carp.protocols.domain.tasks

import dk.cachet.carp.protocols.domain.InvalidConfigurationError
import dk.cachet.carp.protocols.domain.JsIgnore
import dk.cachet.carp.protocols.domain.serialization.PolymorphicArrayListSerializer
import dk.cachet.carp.protocols.domain.tasks.measures.Measure
import kotlinx.serialization.Serializable
import kotlin.test.*


/**
 * Tests for [TaskDescriptor].
 */
class TaskDescriptorTest
{
    @Test
    @JsIgnore
    fun mutable_implementation_triggers_exception()
    {
        class NoDataClass(
            override val name: String = "Not a data class",
            // TODO: Use the following serializer in JVM.
            //@Serializable( MeasuresSerializer::class )
            @Serializable( PolymorphicArrayListSerializer::class )
            override val measures: List<Measure> = listOf() ) : TaskDescriptor()

        assertFailsWith<InvalidConfigurationError>
        {
            NoDataClass()
        }
    }
}