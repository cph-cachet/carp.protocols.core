import { expect } from 'chai'
import VerifyModule from './VerifyModule'

import { kotlin } from 'kotlin'
import Pair = kotlin.Pair
import ArrayList = kotlin.collections.ArrayList
import HashSet = kotlin.collections.HashSet
import toMap = kotlin.collections.toMap_v2dak7$
import toSet = kotlin.collections.toSet_us0mfu$

import { kotlinx } from 'kotlinx-serialization-kotlinx-serialization-json-js-legacy'
import Json = kotlinx.serialization.json.Json
import { kotlinx as kotlinxcore } from 'kotlinx-serialization-kotlinx-serialization-core-js-legacy'
import ListSerializer = kotlinxcore.serialization.builtins.ListSerializer_swdriu$

import { dk as cdk } from 'carp.core-kotlin-carp.common'
import DateTime = cdk.cachet.carp.common.application.DateTime
import NamespacedId = cdk.cachet.carp.common.application.NamespacedId
import UUID = cdk.cachet.carp.common.application.UUID
import CarpInputDataTypes = cdk.cachet.carp.common.application.data.input.CarpInputDataTypes
import Text = cdk.cachet.carp.common.application.data.input.elements.Text
import ParticipantAttribute = cdk.cachet.carp.common.application.users.ParticipantAttribute
import Username = cdk.cachet.carp.common.application.users.Username
import UsernameAccountIdentity = cdk.cachet.carp.common.application.users.UsernameAccountIdentity
import createDefaultJSON = cdk.cachet.carp.common.infrastructure.serialization.createDefaultJSON_18xi4u$

import { dk as ddk } from 'carp.core-kotlin-carp.deployments.core'
import StudyDeploymentStatus = ddk.cachet.carp.deployments.application.StudyDeploymentStatus
import StudyInvitation = ddk.cachet.carp.deployments.application.users.StudyInvitation

import { dk } from 'carp.core-kotlin-carp.studies.core'
import StudyDetails = dk.cachet.carp.studies.application.StudyDetails
import StudyStatus = dk.cachet.carp.studies.application.StudyStatus
import AssignParticipantDevices = dk.cachet.carp.studies.application.users.AssignParticipantDevices
import Participant = dk.cachet.carp.studies.application.users.Participant
import ParticipantGroupStatus = dk.cachet.carp.studies.application.users.ParticipantGroupStatus
import StudyOwner = dk.cachet.carp.studies.application.users.StudyOwner
import getAssignedParticipantIds = dk.cachet.carp.studies.application.users.participantIds_ttprz$
import getAssignedDeviceRoles = dk.cachet.carp.studies.application.users.deviceRoles_ttprz$
import RecruitmentServiceRequest = dk.cachet.carp.studies.infrastructure.RecruitmentServiceRequest
import StudyServiceRequest = dk.cachet.carp.studies.infrastructure.StudyServiceRequest


describe( "carp.studies.core", () => {
    it( "verify module declarations", async () => {
        const instances = [
            new StudyDetails( UUID.Companion.randomUUID(), new StudyOwner(), "Name", DateTime.Companion.now(), "Description", StudyInvitation.Companion.empty(), null ),
            StudyDetails.Companion,
            new StudyStatus.Configuring( UUID.Companion.randomUUID(), "Test", DateTime.Companion.now(), true, true, false, true ),
            new StudyStatus.Live( UUID.Companion.randomUUID(), "Test", DateTime.Companion.now(), false, false, true ),
            StudyStatus.Companion,
            new AssignParticipantDevices( UUID.Companion.randomUUID(), toSet( [ "Test" ] ) ),
            AssignParticipantDevices.Companion,
            new Participant( new UsernameAccountIdentity( new Username( "Test" ) ) ),
            Participant.Companion,
            new ParticipantGroupStatus( new StudyDeploymentStatus(), new HashSet<Participant>() ),
            ParticipantGroupStatus.Companion,
            new StudyOwner(),
            StudyOwner.Companion,
            RecruitmentServiceRequest.Companion,
            StudyServiceRequest.Companion,
        ]

        const moduleVerifier = new VerifyModule( 'carp.core-kotlin-carp.studies.core', instances )
        await moduleVerifier.verify()
    } )


    describe( "AssignParticipantDevices", () => {
        it( "initializes deviceRoleNames as set", () => {
            const uuid = UUID.Companion.randomUUID()
            const masterDeviceRoleNames = toSet( [ "Test" ] )
            const assigned = new AssignParticipantDevices( uuid, masterDeviceRoleNames )
            expect( assigned.masterDeviceRoleNames ).equals( masterDeviceRoleNames )
        } )

        it( "getAssigned participantIds and devices works", () => {
            const participant1 = UUID.Companion.randomUUID()
            const participant2 = UUID.Companion.randomUUID()
            const assigned1 = new AssignParticipantDevices( participant1, toSet( [ "Test" ] ) )
            const assigned2 = new AssignParticipantDevices( participant2, toSet( [ "Test" ] ) )
            const assignedGroup = new ArrayList( [ assigned1, assigned2 ] )
            expect( getAssignedParticipantIds( assignedGroup ) ).instanceof( HashSet )
            expect( getAssignedDeviceRoles( assignedGroup ) ).instanceof( HashSet )
        } )
    } )


    describe( "StudyOwner", () => {
        it( "initializes with default id", () => {
            const owner = new StudyOwner()
            expect( owner.id ).instanceOf( UUID )
        } )
    } )


    describe( "StudyStatus", () => {
        it ( "can typecheck StudyStatus", () => {
            const configuring = new StudyStatus.Configuring( UUID.Companion.randomUUID(), "Test", DateTime.Companion.now(), true, true, false, true )
            const configuringStatus: StudyStatus = configuring
            expect( configuringStatus instanceof StudyStatus.Configuring ).is.true
            expect( configuringStatus instanceof StudyStatus.Live ).is.false

            const live = new StudyStatus.Live( UUID.Companion.randomUUID(), "Test", DateTime.Companion.now(), false, false, true )
            const liveStatus: StudyStatus = live
            expect( liveStatus instanceof StudyStatus.Live ).is.true
            expect( liveStatus instanceof StudyStatus.Configuring ).is.false
        } )
    } )


    describe( "StudyServiceRequest", () => {
        it( "can serialize requests with polymorphic serializer", () => {
            const createStudy = new StudyServiceRequest.CreateStudy(
                new StudyOwner(),
                "Test study",
                "This is a study description",
                StudyInvitation.Companion.empty()
            )

            const json: Json = createDefaultJSON()
            const serializer = StudyServiceRequest.Companion.serializer()
            const serialized = json.encodeToString_tf03ej$( serializer, createStudy )
            expect( serialized ).has.string( "dk.cachet.carp.studies.infrastructure.StudyServiceRequest.CreateStudy" )
        } )

        it( "can serialize getStudiesOverview response", () => {
            const status = new StudyStatus.Configuring( UUID.Companion.randomUUID(), "Test", DateTime.Companion.now(), true, true, false, true )
            const statusList = new ArrayList( [ status ] )

            const json: Json = createDefaultJSON()
            const serializer = ListSerializer( StudyStatus.Companion.serializer() )
            expect( serializer ).is.not.undefined
            const serialized = json.encodeToString_tf03ej$( serializer, statusList )
            expect( serialized ).is.not.not.undefined
        } )
    } )


    describe( "RecruitmentServiceRequest", () => {
        it( "can serialize DeployParticipantGroup", () => {
            const deployGroup = new RecruitmentServiceRequest.DeployParticipantGroup(
                UUID.Companion.randomUUID(),
                toSet( [
                    new AssignParticipantDevices( UUID.Companion.randomUUID(), toSet( [ "Smartphone" ] ) )
                ] )
            )

            const json: Json = createDefaultJSON()
            const serializer = RecruitmentServiceRequest.Companion.serializer()
            const serialized = json.encodeToString_tf03ej$( serializer, deployGroup )
            expect( serialized ).is.not.undefined
        } )

        it( "can serialize Participant", () => {
            const participant = new Participant( new UsernameAccountIdentity( new Username( "Test" ) ), UUID.Companion.randomUUID() )

            const json: Json = createDefaultJSON()
            const serializer = Participant.Companion.serializer()
            const serialized = json.encodeToString_tf03ej$( serializer, participant )
            expect( serialized ).is.not.undefined
        } )

        it( "can serialize ParticipantGroupStatus", () => {
            const deploymentStatus = new StudyDeploymentStatus.DeploymentReady( UUID.Companion.randomUUID(), new ArrayList( [] ), DateTime.Companion.now() )
            const participants = toSet( [ new Participant( new UsernameAccountIdentity( new Username( "Test" ) ) ) ] )
            const group = new ParticipantGroupStatus( deploymentStatus, participants )

            const json: Json = createDefaultJSON()
            const serializer = ParticipantGroupStatus.Companion.serializer()
            const serialized = json.encodeToString_tf03ej$( serializer, group )
            expect( serialized ).is.not.undefined
        } )
    } )
} )
