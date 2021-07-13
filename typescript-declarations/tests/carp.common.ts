import { expect } from 'chai'
import VerifyModule from './VerifyModule'

import { kotlin } from 'kotlin'
import { Long } from 'kotlin'
import toSet = kotlin.collections.toSet_us0mfu$

import { kotlinx } from 'kotlinx-serialization-kotlinx-serialization-json-js-legacy'
import Json = kotlinx.serialization.json.Json

import { dk } from 'carp.core-kotlin-carp.common'
import DateTime = dk.cachet.carp.common.application.DateTime
import EmailAddress = dk.cachet.carp.common.application.EmailAddress
import NamespacedId = dk.cachet.carp.common.application.NamespacedId
import TimeSpan = dk.cachet.carp.common.application.TimeSpan
import Trilean = dk.cachet.carp.common.application.Trilean
import UUID = dk.cachet.carp.common.application.UUID
import toTrilean = dk.cachet.carp.common.application.toTrilean_1v8dcc$
import DefaultDeviceRegistration = dk.cachet.carp.common.application.devices.DefaultDeviceRegistration
import CarpInputDataTypes = dk.cachet.carp.common.application.data.input.CarpInputDataTypes
import SelectOne = dk.cachet.carp.common.application.data.input.elements.SelectOne
import Text = dk.cachet.carp.common.application.data.input.elements.Text
import DeviceRegistration = dk.cachet.carp.common.application.devices.DeviceRegistration
import AccountIdentity = dk.cachet.carp.common.application.users.AccountIdentity
import EmailAccountIdentity = dk.cachet.carp.common.application.users.EmailAccountIdentity
import ParticipantAttribute = dk.cachet.carp.common.application.users.ParticipantAttribute
import Username = dk.cachet.carp.common.application.users.Username
import UsernameAccountIdentity = dk.cachet.carp.common.application.users.UsernameAccountIdentity
import emailAccountIdentityFromString = dk.cachet.carp.common.application.users.EmailAccountIdentity_init_61zpoe$
import createDefaultJSON = dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON_18xi4u$


describe( "carp.common", () => {
    it( "verify module declarations", async () => {
        const username = new Username( "Test" )

        const instances = [
            DateTime.Companion.now(),
            DateTime.Companion,
            new EmailAddress( "test@test.com" ),
            EmailAddress.Companion,
            new NamespacedId( "namespace", "type" ),
            NamespacedId.Companion,
            TimeSpan.Companion.INFINITE,
            TimeSpan.Companion,
            UUID.Companion.randomUUID(),
            UUID.Companion,
            [ "InputElement", new Text( "How are you feeling?" ) ],
            new SelectOne( "Sex", toSet( [ "Male", "Female" ] ) ),
            SelectOne.Companion,
            new Text( "How are you feeling?" ),
            Text.Companion,
            [ "DeviceRegistration", new DefaultDeviceRegistration( "some device id" ) ],
            DeviceRegistration.Companion,
            AccountIdentity.Factory,
            new EmailAccountIdentity( new EmailAddress( "test@test.com" ) ),
            EmailAccountIdentity.Companion,
            username,
            Username.Companion,
            new UsernameAccountIdentity( username ),
            UsernameAccountIdentity.Companion,
            [ "ParticipantAttribute", new ParticipantAttribute.DefaultParticipantAttribute( new NamespacedId( "namespace", "type" ) ) ],
            ParticipantAttribute.Companion,
        ]

        const moduleVerifier = new VerifyModule( 'carp.core-kotlin-carp.common', instances )
        await moduleVerifier.verify()
    } )


    describe( "DateTime", () => {
        it( "serializes as string", () => {
            const dateTime = new DateTime( Long.fromNumber( 42 ) )
            
            const json: Json = createDefaultJSON()
            const serializer = DateTime.Companion.serializer()
            const serialized = json.encodeToString_tf03ej$( serializer, dateTime )
    
            expect( serialized ).equals( "\"1970-01-01T00:00:00.042Z\"" )
        } )
    
        it( "msSinceUTC is Long", () => {
            const now = DateTime.Companion.now()
    
            expect( now.msSinceUTC ).instanceOf( Long )
        } )
    } )


    describe( "TimeSpan", () => {
        it( "totalMilliseconds works", () => {
            const second = new TimeSpan( Long.fromNumber( 1000000 ) )
            const ms = second.totalMilliseconds
            expect( ms ).equals( 1000 )
        } )
    } )


    describe( "Trilean", () => {
        it( "has values TRUE, FALSE, UNKNOWN", () => {
            const values = Trilean.values()
            expect( values ).to.have.members( [ Trilean.TRUE, Trilean.FALSE, Trilean.UNKNOWN ] )
        } )

        it ( "toTrilean works", () => {
            expect( toTrilean( true ) ).equals( Trilean.TRUE )
            expect( toTrilean( false ) ).equals( Trilean.FALSE )
        } )
    } )


    describe( "EmailAccountIdentity", () => {
        it( "can initialize from string", () => {
            const identity = emailAccountIdentityFromString( "test@test.com" )
            expect( identity.emailAddress ).instanceOf( EmailAddress )
        } )
    } )

    describe( "ParticipantAttribute", () => {
        const attribute = new ParticipantAttribute.CustomParticipantAttribute( new Text( "Name" ) )

        it( "getInputElement works", () => {
            const inputElement = attribute.getInputElement_6eo89k$( CarpInputDataTypes )
            expect( inputElement ).instanceOf( Text )
        } )

        it( "isValidInput works", () => {
            const isNumberValid = attribute.isValidInput_etkzhw$( CarpInputDataTypes, 42 )
            expect( isNumberValid ).is.false

            const isStringValid = attribute.isValidInput_etkzhw$( CarpInputDataTypes, "Steven" )
            expect( isStringValid ).is.true
        } )

        it( "inputToData works", () => {
            const data = attribute.inputToData_etkzhw$( CarpInputDataTypes, "Steven" )
            expect( data ).is.not.undefined
        } )
    } )
} )
