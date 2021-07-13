package dk.cachet.carp.common.infrastructure.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.encodeToHexString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.subclass
import kotlinx.serialization.protobuf.ProtoBuf
import kotlin.test.*


/**
 * Tests for [UnknownPolymorphicSerializer].
 */
class UnknownPolymorphicSerializerTest
{
    private val testModule = SerializersModule {
        polymorphic( BaseType::class )
        {
            subclass( DerivingType::class )

            subclass( CustomBaseType::class )
            default { UnknownBaseTypeSerializer }
        }
    }

    @Serializable
    @Polymorphic
    abstract class BaseType
    {
        abstract val toOverrideProperty: String

        val baseProperty: Boolean = true
    }

    @Serializable
    data class DerivingType( override val toOverrideProperty: String ) : BaseType()

    @Serializable( UnknownBaseTypeSerializer::class )
    data class CustomBaseType( override val className: String, override val jsonSource: String, val serializer: Json ) :
        BaseType(), UnknownPolymorphicWrapper
    {
        @Serializable
        private class BaseMembers( override val toOverrideProperty: String ) : BaseType()

        override val toOverrideProperty: String

        init
        {
            val json = Json( serializer ) { ignoreUnknownKeys = true }
            val baseMembers = json.decodeFromString( BaseMembers.serializer(), jsonSource )
            toOverrideProperty = baseMembers.toOverrideProperty
        }
    }

    object UnknownBaseTypeSerializer : KSerializer<BaseType>
        by createUnknownPolymorphicSerializer( { className, json, serializer -> CustomBaseType( className, json, serializer ) } )


    private fun initializeJson( classDiscriminator: String = "type" ) = Json {
        this.classDiscriminator = classDiscriminator
        serializersModule = testModule
        // TODO: `encodeDefaults` changed in kotlinx.serialization 1.0.0-RC2 to false by default
        //  which caused unknown polymorphic serializer tests to fail. Verify whether we need this.
        encodeDefaults = true
    }

    @Test
    fun base_properties_are_serialized()
    {
        val json = initializeJson()
        val knownType = DerivingType( "Test" )

        val serialized = json.encodeToString<BaseType>( knownType )

        assertTrue( serialized.contains( BaseType::baseProperty.name ) )
    }

    @Test
    fun unknown_types_are_wrapped_when_deserializing()
    {
        val json = initializeJson()
        val toSerialize = DerivingType( "Test" )
        val serialized = json.encodeToString<BaseType>( toSerialize )
        val unknownSerialized = serialized.replace( DerivingType::class.simpleName!!, "UnknownType" )

        val parsed = json.decodeFromString( BaseType.serializer(), unknownSerialized )
        assertTrue( parsed is CustomBaseType )
        assertEquals( "dk.cachet.carp.common.infrastructure.serialization.UnknownPolymorphicSerializerTest.UnknownType", parsed.className )
        val expectedJsonSource = json.encodeToString<BaseType>( toSerialize )
            .replace( DerivingType::class.simpleName!!, "UnknownType" )
        assertEquals( expectedJsonSource, parsed.jsonSource )
    }

    @Test
    fun unknown_types_are_unpacked_when_serializing()
    {
        val json = initializeJson()

        // Create unknown type as represented at runtime.
        val knownType = DerivingType( "Test" )
        val serialized = json.encodeToString<BaseType>( knownType )
        val unknownSerialized = serialized.replace( DerivingType::class.simpleName!!, "UnknownType" )
        val unknown = CustomBaseType( "dk.cachet.carp.common.infrastructure.serialization.UnknownPolymorphicSerializerTest.UnknownType", unknownSerialized, json )

        val unpacked = json.encodeToString<BaseType>( unknown )
        assertEquals( unknownSerialized, unpacked )
    }

    @Test
    fun custom_class_discriminator_is_supported()
    {
        val classDiscriminator = "---type---"
        val json = initializeJson( classDiscriminator )
        val toSerialize = DerivingType( "Test" )

        val serialized = json.encodeToString<BaseType>( toSerialize )
        assertTrue( serialized.contains( classDiscriminator ) )

        val unknownSerialized = serialized.replace( DerivingType::class.simpleName!!, "UnknownType" )
        val parsed = json.decodeFromString( BaseType.serializer(), unknownSerialized )
        assertTrue( parsed is CustomBaseType )
    }

    @Test
    fun decoding_unknown_types_fails_when_array_polymorphism_is_configured()
    {
        val invalidJson = Json {
            useArrayPolymorphism = true
            serializersModule = testModule
        }
        val toSerialize = DerivingType( "Test" )
        val serialized = invalidJson.encodeToString<BaseType>( toSerialize )
        val unknownSerialized = serialized.replace( DerivingType::class.simpleName!!, "UnknownType" )

        assertFailsWith<SerializationException> { invalidJson.decodeFromString( BaseType.serializer(), unknownSerialized ) }
    }

    @Test
    @ExperimentalSerializationApi
    fun supports_non_json_encoders_for_known_types()
    {
        val toSerialize = DerivingType( "Test" )

        val protobuf = ProtoBuf { serializersModule = testModule }
        val encoded = protobuf.encodeToHexString<BaseType>( toSerialize )
        val decoded = protobuf.decodeFromHexString( BaseType.serializer(), encoded )
        assertEquals( toSerialize, decoded )
    }

    @Test
    @ExperimentalSerializationApi
    fun does_not_support_non_json_encoders_for_unknown_types()
    {
        class UnregisteredType( override val toOverrideProperty: String ) : BaseType()
        val unregistered = UnregisteredType( "Test" )

        val protobuf = ProtoBuf { serializersModule = testModule }
        assertFailsWith<SerializationException> { protobuf.encodeToHexString<BaseType>( unregistered ) }
    }
}
