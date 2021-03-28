import VerifyModule from './VerifyModule'

import { kotlin } from 'kotlin'
import ArrayList = kotlin.collections.ArrayList
import toSet = kotlin.collections.toSet_us0mfu$
import toMap = kotlin.collections.toMap_v2dak7$
import { dk as dkc } from 'carp.core-kotlin-carp.common'
import UUID = dkc.cachet.carp.common.application.UUID
import { dk as dkp } from 'carp.core-kotlin-carp.protocols.core'
import Smartphone = dkp.cachet.carp.protocols.domain.devices.Smartphone
import DefaultDeviceRegistration = dkp.cachet.carp.protocols.domain.devices.DefaultDeviceRegistration
import { dk } from 'carp.core-kotlin-carp.deployment.core'
import ActiveParticipationInvitation = dk.cachet.carp.deployment.domain.users.ActiveParticipationInvitation
import AssignedMasterDevice = dk.cachet.carp.deployment.domain.users.AssignedMasterDevice
import ParticipantData = dk.cachet.carp.deployment.domain.users.ParticipantData
import Participation = dk.cachet.carp.deployment.domain.users.Participation
import StudyInvitation = dk.cachet.carp.deployment.domain.users.StudyInvitation
import DeviceDeploymentStatus = dk.cachet.carp.deployment.domain.DeviceDeploymentStatus
import MasterDeviceDeployment = dk.cachet.carp.deployment.domain.MasterDeviceDeployment
import StudyDeploymentStatus = dk.cachet.carp.deployment.domain.StudyDeploymentStatus
import DeploymentServiceRequest = dk.cachet.carp.deployment.infrastructure.DeploymentServiceRequest
import ParticipationServiceRequest = dk.cachet.carp.deployment.infrastructure.ParticipationServiceRequest


describe( "carp.deployment.core", () => {
    it( "verify module declarations", async () => {
        const exampleDevice = new Smartphone( "test", toMap( [] ) )
        const instances = [
            new ActiveParticipationInvitation( new Participation( UUID.Companion.randomUUID() ), StudyInvitation.Companion.empty(), toSet( [] ) ),
            ActiveParticipationInvitation.Companion,
            new AssignedMasterDevice( exampleDevice, null ),
            AssignedMasterDevice.Companion,
            new ParticipantData( UUID.Companion.randomUUID(), toMap( [] ) ),
            ParticipantData.Companion,
            new Participation( UUID.Companion.randomUUID() ),
            Participation.Companion,
            StudyInvitation.Companion.empty(),
            StudyInvitation.Companion,
            DeviceDeploymentStatus.Companion,
            new DeviceDeploymentStatus.Unregistered( null, true, toSet( [] ), toSet( [] ) ),
            new DeviceDeploymentStatus.Registered( null, true, toSet( [] ), toSet( [] ) ),
            new DeviceDeploymentStatus.Deployed( null ),
            new DeviceDeploymentStatus.NeedsRedeployment( null, toSet( [] ), toSet( [] ) ),
            [ "NotDeployed", new DeviceDeploymentStatus.Unregistered( null, true, toSet( [] ), toSet( [] ) ) ],
            StudyDeploymentStatus.Companion,
            new MasterDeviceDeployment(
                exampleDevice,
                new DefaultDeviceRegistration( "some role" ),
                toSet( [] ), toMap( [] ), toSet( [] ), toMap( [] ), toSet( [] ) ),
            MasterDeviceDeployment.Companion,
            new StudyDeploymentStatus.Invited( UUID.Companion.randomUUID(), new ArrayList<DeviceDeploymentStatus>( [] ), null ),
            new StudyDeploymentStatus.DeployingDevices( UUID.Companion.randomUUID(), new ArrayList<DeviceDeploymentStatus>( [] ), null ),
            new StudyDeploymentStatus.DeploymentReady( UUID.Companion.randomUUID(), new ArrayList<DeviceDeploymentStatus>( [] ), null ),
            new StudyDeploymentStatus.Stopped( UUID.Companion.randomUUID(), new ArrayList<DeviceDeploymentStatus>( [] ), null ),
            DeploymentServiceRequest.Companion,
            ParticipationServiceRequest.Companion
        ]

        const moduleVerifier = new VerifyModule( 'carp.core-kotlin-carp.deployment.core', instances )
        await moduleVerifier.verify()
    } )
} )
