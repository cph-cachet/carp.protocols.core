@file:Suppress( "FunctionName" )

package dk.cachet.carp.test.serialization

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleCollector
import kotlin.reflect.KClass
import kotlin.test.*


/**
 * A base test class to verify whether serialization for all supported types in a specified [serialModule] succeed.
 */
@Suppress( "UnnecessaryAbstractClass" ) // When turned into an open class, mocha tries to run these tests (on the base class).
abstract class ConcreteTypesSerializationTest(
    /**
     * The JSON serializer to use.
     */
    protected val json: Json,
    /**
     * The serial module containing the concrete types for which serialization needs to be tested.
     */
    private val serialModule: SerializersModule,
    /**
     * For each of the types registered in [serialModule], an instance to be used to test serialization.
     */
    private val instancesToSerialize: List<Any>
)
{
    @ExperimentalSerializationApi
    @Test
    fun all_types_in_serial_module_are_tested()
    {
        val typesToTest = getPolymorphicSerializers( serialModule ).keys
        for ( type in typesToTest )
        {
            val typeIsTested = instancesToSerialize.any { it::class == type }
            assertTrue( typeIsTested, "Missing instance of type '$type' to test serialization." )
        }
    }

    @ExperimentalSerializationApi
    @Test
    fun can_serialize_all_instances_using_JSON()
    {
        val polymorphicSerializers = getPolymorphicSerializers( serialModule )
        for ( toSerialize in instancesToSerialize )
        {
            // Get serializer.
            val type = toSerialize::class
            @Suppress( "UNCHECKED_CAST" )
            val serializer = polymorphicSerializers[ type ] as? KSerializer<Any>
            assertNotNull( serializer, "No serializer registered for type '$type'" )

            // Verify whether serializing and deserializing the instance results in the same object.
            val serialized = json.encodeToString( serializer, toSerialize )
            val parsed = json.decodeFromString( serializer, serialized )
            assertEquals( toSerialize, parsed, "Serialization of type '$type' failed." )
        }
    }
}


/**
 * Get a map which holds the [KSerializer] for each [KClass] registered for polymorphic serialization in [serialModule].
 */
@ExperimentalSerializationApi
fun getPolymorphicSerializers( serialModule: SerializersModule ): Map<KClass<*>, KSerializer<*>>
{
    val collector =
        object : SerializersModuleCollector
        {
            val serializers: MutableMap<KClass<*>, KSerializer<*>> = mutableMapOf()

            override fun <T : Any> contextual(
                kClass: KClass<T>,
                provider: (typeArgumentsSerializers: List<KSerializer<*>>) -> KSerializer<*>
            ) = throw UnsupportedOperationException()

            override fun <Base : Any, Sub : Base> polymorphic(
                baseClass: KClass<Base>,
                actualClass: KClass<Sub>,
                actualSerializer: KSerializer<Sub>
            )
            {
                serializers[ actualClass ] = actualSerializer
            }

            override fun <Base : Any> polymorphicDefault(
                baseClass: KClass<Base>,
                defaultSerializerProvider: (className: String?) -> DeserializationStrategy<out Base>?
            )
            {
                // The default serializer is not returned by this method.
            }
        }

    serialModule.dumpTo( collector )
    return collector.serializers
}
