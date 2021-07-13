declare module 'carp.core-kotlin-carp.common'
{
    import { kotlin } from 'kotlin'
    import { Long } from 'kotlin'
    import ArrayList = kotlin.collections.ArrayList
    import HashMap = kotlin.collections.HashMap
    import HashSet = kotlin.collections.HashSet

    import { kotlinx } from 'kotlinx-serialization-kotlinx-serialization-json-js-legacy'
    import Json = kotlinx.serialization.json.Json
    
    
    namespace dk.cachet.carp.common.application
    {
        import ParticipantAttribute = dk.cachet.carp.common.application.users.ParticipantAttribute


        class DateTime
        {
            constructor( msSinceUTC: Long )

            static get Companion(): DateTime$Companion

            readonly msSinceUTC: Long
            toString(): string
        }
        interface DateTime$Companion
        {
            serializer(): any;
            now(): DateTime;
            fromString_61zpoe$( s: string ): DateTime
        }


        class EmailAddress
        {
            constructor( address: string )

            static get Companion(): EmailAddress$Companion

            readonly address: string
        }
        interface EmailAddress$Companion { serializer(): any }


        class NamespacedId
        {
            constructor( namespace: string, name: string )

            static get Companion(): NamespacedId$Companion

            readonly namespace: string
            readonly name: string
        }
        interface NamespacedId$Companion { serializer(): any }


        class TimeSpan
        {
            constructor( microseconds: Long )

            static get Companion(): TimeSpan$Companion

            get totalMilliseconds(): number
        }
        interface TimeSpan$Companion
        {
            readonly INFINITE: TimeSpan;
            serializer(): any;
            fromMilliseconds_14dthe$( ms: number ): TimeSpan;
        }


        class Trilean
        {
            static readonly TRUE: Trilean
            static readonly FALSE: Trilean
            static readonly UNKNOWN: Trilean
            static values(): Array<Trilean>
        }
        function toTrilean_1v8dcc$( bool: boolean ): Trilean


        class UUID
        {
            constructor( stringRepresentation: string )

            static get Companion(): UUID$Companion

            readonly stringRepresentation: string
        }
        interface UUID$Companion
        {
            serializer(): any;
            randomUUID(): UUID;
        }
    }


    namespace dk.cachet.carp.common.application.devices
    {
        abstract class DeviceRegistration
        {
            static get Companion(): DeviceRegistration$Companion  
            
            readonly deviceId: string
            readonly registrationCreationDate: DateTime
        }
        interface DeviceRegistration$Companion { serializer(): any }

        class DefaultDeviceRegistration extends DeviceRegistration
        {
            constructor( deviceId: string )
        }

        class Smartphone
        {
            constructor( roleName: string, defaultSamplingConfiguration: HashMap<NamespacedId, any> )
        }
    }


    namespace dk.cachet.carp.common.application.users
    {
        import InputElement = dk.cachet.carp.common.application.data.input.elements.InputElement
        import InputDataTypeList = dk.cachet.carp.common.application.data.input.InputDataTypeList


        class Username
        {
            constructor( name: string )

            static get Companion(): Username$Companion

            readonly name: string
        }
        interface Username$Companion { serializer(): any }

        abstract class AccountIdentity
        {
            static get Factory(): AccountIdentity$Factory
        }
        interface AccountIdentity$Factory
        {
            fromEmailAddress_61zpoe$( emailAddress: string ): EmailAccountIdentity;
            fromUsername_61zpoe$( username: string ): UsernameAccountIdentity;
        }

        class EmailAccountIdentity extends AccountIdentity
        {
            constructor( emailAddress: EmailAddress )

            static get Companion(): EmailAccountIdentity$Companion

            readonly emailAddress: EmailAddress
        }
        function EmailAccountIdentity_init_61zpoe$( emailAddress: string ): EmailAccountIdentity
        interface EmailAccountIdentity$Companion { serializer(): any }

        class UsernameAccountIdentity extends AccountIdentity
        {
            constructor( username: Username )

            static get Companion(): UsernameAccountIdentity$Companion

            readonly username: Username
        }
        function UsernameAccountIdentity_init_61zpoe$( username: string ): UsernameAccountIdentity
        interface UsernameAccountIdentity$Companion { serializer(): any }


        abstract class ParticipantAttribute
        {
            static get Companion(): ParticipantAttribute$Companion

            readonly inputType: NamespacedId

            getInputElement_6eo89k$( registeredInputDataTypes: InputDataTypeList ): InputElement
            isValidInput_etkzhw$( registeredInputDataTypes: InputDataTypeList, input: any ): boolean
            inputToData_etkzhw$( registeredInputDataTypes: InputDataTypeList, input: any ): any
            isValidData_bq34fz$( registeredInputDataTypes: InputDataTypeList, data: any ): boolean
            dataToInput_bq34fz$( registeredInputDataTypes: InputDataTypeList, data: any ): any
        }
        interface ParticipantAttribute$Companion { serializer(): any }

        namespace ParticipantAttribute
        {
            class DefaultParticipantAttribute extends ParticipantAttribute
            {
                constructor( inputType: NamespacedId )
            }

            class CustomParticipantAttribute extends ParticipantAttribute
            {
                constructor( input: InputElement )
            }
        }
    }


    namespace dk.cachet.carp.common.application.data.input
    {
        // No need to initialize this from TypeScript right now. Access to `CarpInputDataTypes` is sufficient.
        class InputDataTypeList { constructor() }
        const CarpInputDataTypes: InputDataTypeList
    }

    namespace dk.cachet.carp.common.application.data.input.elements
    {
        interface InputElement
        {
            readonly name: string

            isValid_trkh7z$( input: any ): boolean
        }
        
        class Text implements InputElement
        {
            constructor( name: string )

            static get Companion(): Text$Companion

            readonly name: string
            isValid_trkh7z$( input: any ): boolean
        }
        interface Text$Companion { serializer(): any }

        class SelectOne implements InputElement
        {
            constructor( name: string, options: HashSet<string> )

            static get Companion(): SelectOne$Companion

            readonly name: string
            readonly options: HashSet<string>
            isValid_trkh7z$( input: any ): boolean
        }
        interface SelectOne$Companion { serializer(): any }
    }


    namespace dk.cachet.carp.common.infrastructure.serialization
    {
        function createDefaultJSON_18xi4u$(): Json
    }
}
