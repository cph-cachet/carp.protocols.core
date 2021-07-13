package dk.cachet.carp.common.application

import dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON
import kotlinx.serialization.Serializable
import kotlin.test.*


/**
 * Tests for [UUID].
 */
class UUIDTest
{
    @Test
    fun can_serialize_and_deserialize_UUID_using_JSON()
    {
        val id = UUID( "00000000-0000-0000-0000-000000000000" )

        val json = createDefaultJSON()
        val serialized = json.encodeToString( UUID.serializer(), id )
        val parsed = json.decodeFromString( UUID.serializer(), serialized )

        assertEquals( id, parsed )
    }

    @Serializable
    data class Id( val id: UUID? )

    @Test
    fun can_serialize_and_deserialize_nullable_UUID()
    {
        val json = createDefaultJSON()

        val id = Id( UUID( "00000000-0000-0000-0000-000000000000" ) )
        val idSerialized = json.encodeToString( Id.serializer(), id )
        val idParsed = json.decodeFromString( Id.serializer(), idSerialized )
        assertEquals( id, idParsed )

        val nullableId = Id( null )
        val nullableSerialized = json.encodeToString( Id.serializer(), nullableId )
        val nullableParsed = json.decodeFromString( Id.serializer(), nullableSerialized )
        assertEquals( nullableId, nullableParsed )
    }

    @Test
    fun cant_initialize_incorrect_UUID()
    {
        assertFailsWith<IllegalArgumentException> { UUID( "Invalid" ) }
        // Not long enough.
        assertFailsWith<IllegalArgumentException> { UUID( "00000000-0000-0000-0000-00000000000" ) }
        // Invalid character.
        assertFailsWith<IllegalArgumentException> { UUID( "g0000000-0000-0000-0000-000000000000" ) }
        // Incorrect dashes.
        assertFailsWith<IllegalArgumentException> { UUID( "00000000-0000-00000-0000-00000000000" ) }
        // Non-standard upper case format.
        assertFailsWith<IllegalArgumentException> { UUID( "AAAAAAAA-0000-0000-0000-000000000000" ) }
    }

    @Test
    fun parse_succeeds_with_alternate_representations()
    {
        val upperCase = UUID.parse( "AAAAAAAA-0000-0000-0000-000000000000" )
        assertEquals( "aaaaaaaa-0000-0000-0000-000000000000", upperCase.stringRepresentation )
    }
}
