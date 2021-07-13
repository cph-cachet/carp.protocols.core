package dk.cachet.carp.common.application.data.input

import dk.cachet.carp.common.application.data.Data
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.serializer
import kotlin.reflect.KClass


/**
 * Holds custom input data as requested by a researcher.
 */
@Serializable( CustomInputSerializer::class )
@SerialName( CUSTOM_INPUT_TYPE_NAME )
@Suppress( "Immutable" ) // TODO: `assumeImmutable` configuration in detekt.yml is not working.
data class CustomInput( val input: Any ) : Data


/**
 * A serializer for [CustomInput] which supports serializing input of registered data types.
 *
 * @param supportedDataTypes The input data types this serializer can serialize.
 *   A serializer should be registered for these classes.
 */
class CustomInputSerializer( vararg supportedDataTypes: KClass<*> ) : KSerializer<CustomInput>
{
    @InternalSerializationApi
    // TODO: Can we use the fully qualified type name to register serializers here, like kotlinx.serialization does? How?
    val dataTypeMap: Map<String, KSerializer<out Any>> = supportedDataTypes.map { it.simpleName!! to it.serializer() }.toMap()

    @ExperimentalSerializationApi
    @InternalSerializationApi
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor( CUSTOM_INPUT_TYPE_NAME )
    {
        element<String>( "dataType" )
        element( "input", buildSerialDescriptor("$CUSTOM_INPUT_TYPE_NAME.value", SerialKind.CONTEXTUAL ) )
    }

    @ExperimentalSerializationApi
    @InternalSerializationApi
    override fun deserialize( decoder: Decoder ): CustomInput =
        decoder.decodeStructure( descriptor )
        {
            // Read dataType.
            val index = decodeElementIndex( descriptor )
            require( index == 0 ) { "Cannot read input before its data type." }
            val dataType = decodeStringElement( descriptor, 0 )

            // Read input
            decodeElementIndex( descriptor )
            val serializer = getRegisteredSerializer( dataType )
            val input = decodeSerializableElement( descriptor, 1, serializer )

            return CustomInput( input )
        }

    @ExperimentalSerializationApi
    @InternalSerializationApi
    override fun serialize( encoder: Encoder, value: CustomInput ) =
        encoder.encodeStructure( descriptor )
        {
            val input = value.input
            val inputKlass = input::class.simpleName!!

            encodeStringElement( descriptor, 0, inputKlass )

            @Suppress( "UNCHECKED_CAST" )
            val serializer = getRegisteredSerializer( inputKlass ) as KSerializer<Any>
            encodeSerializableElement( descriptor, 1, serializer, input )
        }

    @InternalSerializationApi
    fun getRegisteredSerializer( dataType: String ): KSerializer<out Any> = dataTypeMap[ dataType ]
        ?: throw UnsupportedOperationException( "No serializer registered for custom input type: $dataType" )
}


internal const val CUSTOM_INPUT_TYPE_NAME = "${CarpInputDataTypes.CARP_NAMESPACE}.custom"
