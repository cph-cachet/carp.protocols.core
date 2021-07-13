import { expect } from 'chai'
import VerifyModule from './VerifyModule'

import { kotlinx } from 'kotlinx-serialization-kotlinx-serialization-json-js-legacy'
import Json = kotlinx.serialization.json.Json

import { dk as cdk } from 'carp.core-kotlin-carp.common'
import UUID = cdk.cachet.carp.common.application.UUID
import createDefaultJSON = cdk.cachet.carp.common.infrastructure.serialization.createDefaultJSON_18xi4u$

import { dk } from 'carp.core-kotlin-carp.protocols.core'
import ProtocolVersion = dk.cachet.carp.protocols.application.ProtocolVersion
import StudyProtocolId = dk.cachet.carp.protocols.application.StudyProtocolId
import StudyProtocolSnapshot = dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import ProtocolOwner = dk.cachet.carp.protocols.domain.ProtocolOwner
import ProtocolFactoryServiceRequest = dk.cachet.carp.protocols.infrastructure.ProtocolFactoryServiceRequest
import ProtocolServiceRequest = dk.cachet.carp.protocols.infrastructure.ProtocolServiceRequest

const serializedSnapshot = `{"id":{"ownerId":"27879e75-ccc1-4866-9ab3-4ece1b735052","name":"Test protocol"},"description":"Test description","creationDate":"2021-06-18T14:28:14.229Z","masterDevices":[{"$type":"dk.cachet.carp.common.infrastructure.test.StubMasterDeviceDescriptor","isMasterDevice":true,"roleName":"Stub master device","defaultSamplingConfiguration":{}}],"connectedDevices":[{"$type":"dk.cachet.carp.common.infrastructure.test.StubDeviceDescriptor","roleName":"Stub device","defaultSamplingConfiguration":{}},{"$type":"dk.cachet.carp.common.infrastructure.test.StubMasterDeviceDescriptor","isMasterDevice":true,"roleName":"Chained master","defaultSamplingConfiguration":{}},{"$type":"dk.cachet.carp.common.infrastructure.test.StubDeviceDescriptor","roleName":"Chained connected","defaultSamplingConfiguration":{}}],"connections":[{"roleName":"Stub device","connectedToRoleName":"Stub master device"},{"roleName":"Chained master","connectedToRoleName":"Stub master device"},{"roleName":"Chained connected","connectedToRoleName":"Chained master"}],"tasks":[{"$type":"dk.cachet.carp.common.infrastructure.test.StubTaskDescriptor","name":"Task","measures":[{"$type":"dk.cachet.carp.common.application.tasks.Measure.DataStream","type":"dk.cachet.carp.stub","overrideSamplingConfiguration":null}],"description":null}],"triggers":{"0":{"$type":"dk.cachet.carp.common.infrastructure.test.StubTrigger","sourceDeviceRoleName":"Stub device","uniqueProperty":"Unique"}},"taskControls":[{"triggerId":0,"taskName":"Task","destinationDeviceRoleName":"Stub master device","control":"Start"}],"expectedParticipantData":[{"$type":"dk.cachet.carp.common.application.users.ParticipantAttribute.DefaultParticipantAttribute","inputType":"some.type"}],"applicationData":""}`


describe( "carp.protocols.core", () => {
    it( "verify module declarations", async () => {
        // Create `StudyProtocolSnapshot` instance.
        const json: Json = createDefaultJSON()
        const serializer = StudyProtocolSnapshot.Companion.serializer()
        const studyProtocolSnapshot = json.decodeFromString_awif5v$( serializer, serializedSnapshot )

        const instances = [
            new ProtocolVersion( "Version" ),
            ProtocolVersion.Companion,
            new StudyProtocolId( UUID.Companion.randomUUID(), "Name" ),
            StudyProtocolId.Companion,
            studyProtocolSnapshot,
            StudyProtocolSnapshot.Companion,
            new ProtocolOwner(),
            ProtocolOwner.Companion,
            ProtocolFactoryServiceRequest.Companion,
            ProtocolServiceRequest.Companion,
        ]

        const moduleVerifier = new VerifyModule( 'carp.core-kotlin-carp.protocols.core', instances )
        await moduleVerifier.verify()
    } )

    describe( "StudyProtocolSnapshot", () => {
        it( "can deserialize", () => {
            const json: Json = createDefaultJSON()
            const serializer = StudyProtocolSnapshot.Companion.serializer()
            const parsed = json.decodeFromString_awif5v$( serializer, serializedSnapshot )
            expect( parsed ).is.instanceOf( StudyProtocolSnapshot )
        } )
    } )

    describe( "ProtocolServiceRequest", () => {
        it( "add request has default version tag", () => {
            const json: Json = createDefaultJSON()
            const serializer = StudyProtocolSnapshot.Companion.serializer()
            const snapshot = json.decodeFromString_awif5v$( serializer, serializedSnapshot )

            const addProtocol = new ProtocolServiceRequest.Add( snapshot )
            const versionTag = (addProtocol as any).versionTag
            expect( versionTag ).equals( "Initial" )
        } )
    })
} )
