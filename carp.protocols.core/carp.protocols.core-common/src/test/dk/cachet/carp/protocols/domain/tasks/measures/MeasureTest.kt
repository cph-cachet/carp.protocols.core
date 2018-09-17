package dk.cachet.carp.protocols.domain.tasks.measures

import dk.cachet.carp.protocols.domain.InvalidConfigurationError
import dk.cachet.carp.protocols.domain.JsIgnore
import dk.cachet.carp.protocols.domain.data.*
import dk.cachet.carp.protocols.domain.serialization.SerializableWith
import kotlin.test.*


/**
 * Tests for [Measure].
 */
class MeasureTest
{
    @Test
    @JsIgnore
    fun mutable_implementation_triggers_exception()
    {
        class NoDataClass(
            @SerializableWith( DataTypeSerializer::class )
            override val type: DataType ) : Measure()

        assertFailsWith<InvalidConfigurationError>
        {
            NoDataClass(StubDataType())
        }
    }
}