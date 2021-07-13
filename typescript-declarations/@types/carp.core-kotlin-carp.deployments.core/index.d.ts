declare module 'carp.core-kotlin-carp.deployments.core'
{
    import { kotlin } from 'kotlin'
    import ArrayList = kotlin.collections.ArrayList
    import HashMap = kotlin.collections.HashMap
    import HashSet = kotlin.collections.HashSet

    import { kotlinx } from 'kotlinx-serialization-kotlinx-serialization-json-js-legacy'
    import Json = kotlinx.serialization.json.Json

    import { dk as cdk } from 'carp.core-kotlin-carp.common'
    import DateTime = cdk.cachet.carp.common.application.DateTime
    import NamespacedId = cdk.cachet.carp.common.application.NamespacedId
    import UUID = cdk.cachet.carp.common.application.UUID
    import DeviceRegistration = cdk.cachet.carp.common.application.devices.DeviceRegistration
    import AccountIdentity = cdk.cachet.carp.common.application.users.AccountIdentity

    import { dk as pdk } from 'carp.core-kotlin-carp.protocols.core'
    import StudyProtocolSnapshot = pdk.cachet.carp.protocols.application.StudyProtocolSnapshot


    namespace dk.cachet.carp.deployments.application
    {
        class DeviceDeploymentStatus
        {
            static get Companion(): DeviceDeploymentStatus$Companion
        }
        interface DeviceDeploymentStatus$Companion { serializer(): any }

        namespace DeviceDeploymentStatus
        {
            interface NotDeployed
            {
                readonly requiresDeployment: Boolean
                readonly isReadyForDeployment: Boolean
                readonly remainingDevicesToRegisterToObtainDeployment: HashSet<String>
                readonly remainingDevicesToRegisterBeforeDeployment: HashSet<String>

            }
            class Unregistered implements NotDeployed
            {
                constructor(
                    device: any,
                    requiresDeployment: Boolean,
                    remainingDevicesToRegisterToObtainDeployment: HashSet<String>,
                    remainingDevicesToRegisterBeforeDeployment: HashSet<String> )

                readonly device: any
                readonly requiresDeployment: Boolean
                readonly canObtainDeviceDeployment: Boolean
                readonly isReadyForDeployment: Boolean
                readonly remainingDevicesToRegisterToObtainDeployment: HashSet<String>
                readonly remainingDevicesToRegisterBeforeDeployment: HashSet<String>
            }
            class Registered implements NotDeployed
            {
                constructor(
                    device: any,
                    requiresDeployment: Boolean,
                    remainingDevicesToRegisterToObtainDeployment: HashSet<String>,
                    remainingDevicesToRegisterBeforeDeployment: HashSet<String> )

                readonly device: any
                readonly requiresDeployment: Boolean
                readonly canObtainDeviceDeployment: Boolean
                readonly isReadyForDeployment: Boolean
                readonly remainingDevicesToRegisterToObtainDeployment: HashSet<String>
                readonly remainingDevicesToRegisterBeforeDeployment: HashSet<String>
            }
            class Deployed
            {
                constructor( device: any )

                readonly device: any
                readonly requiresDeployment: Boolean
                readonly canObtainDeviceDeployment: Boolean
            }
            class NeedsRedeployment implements NotDeployed
            {
                constructor(
                    device: any,
                    remainingDevicesToRegisterToObtainDeployment: HashSet<String>,
                    remainingDevicesToRegisterBeforeDeployment: HashSet<String> )

                readonly device: any
                readonly requiresDeployment: Boolean
                readonly canObtainDeviceDeployment: Boolean
                readonly isReadyForDeployment: Boolean
                readonly remainingDevicesToRegisterToObtainDeployment: HashSet<String>
                readonly remainingDevicesToRegisterBeforeDeployment: HashSet<String>
            }
        }


        class MasterDeviceDeployment
        {
            constructor(
                deviceDescriptor: any,
                configuration: DeviceRegistration,
                connectedDevices: HashSet<any>,
                connectedDeviceConfigurations: HashMap<string, DeviceRegistration>,
                tasks: HashSet<any>,
                triggers: HashMap<number, any>,
                taskControls: HashSet<any>,
                applicationData: String )

                static get Companion(): MasterDeviceDeployment$Companion

                readonly deviceDescriptor: any
                readonly configuration: DeviceRegistration
                readonly connectedDevices: HashSet<any>
                readonly connectedDeviceConfigurations: HashMap<string, DeviceRegistration>
                readonly tasks: HashSet<any>
                readonly triggers: HashMap<number, any>
                readonly taskControls: HashSet<any>
                readonly applicationData: String
        }
        interface MasterDeviceDeployment$Companion { serializer(): any }


        class StudyDeploymentStatus
        {
            static get Companion(): StudyDeploymentStatus$Companion
        }
        interface StudyDeploymentStatus$Companion { serializer(): any }

        namespace StudyDeploymentStatus
        {
            class Invited
            {
                constructor( studyDeploymentId: UUID, devicesStatus: ArrayList<DeviceDeploymentStatus>, startTime: DateTime | null )

                readonly studyDeploymentId: UUID
                readonly devicesStatus: ArrayList<DeviceDeploymentStatus>
                readonly startTime: DateTime | null
            }
            class DeployingDevices
            {
                constructor( studyDeploymentId: UUID, devicesStatus: ArrayList<DeviceDeploymentStatus>, startTime: DateTime | null  )

                readonly studyDeploymentId: UUID
                readonly devicesStatus: ArrayList<DeviceDeploymentStatus>
                readonly startTime: DateTime | null
            }
            class DeploymentReady
            {
                constructor( studyDeploymentId: UUID, devicesStatus: ArrayList<DeviceDeploymentStatus>, startTime: DateTime | null  )

                readonly studyDeploymentId: UUID
                readonly devicesStatus: ArrayList<DeviceDeploymentStatus>
                readonly startTime: DateTime | null
            }
            class Stopped
            {
                constructor( studyDeploymentId: UUID, devicesStatus: ArrayList<DeviceDeploymentStatus>, startTime: DateTime | null  )

                readonly studyDeploymentId: UUID
                readonly devicesStatus: ArrayList<DeviceDeploymentStatus>
                readonly startTime: DateTime | null
            }
        }
    }


    namespace dk.cachet.carp.deployments.application.users
    {
        class ActiveParticipationInvitation
        {
            constructor( participation: Participation, invitation: StudyInvitation, assignedDevices: HashSet<AssignedMasterDevice> )

            static get Companion(): ActiveParticipationInvitation$Companion

            readonly participation: Participation
            readonly invitation: StudyInvitation
            readonly assignedDevices: HashSet<AssignedMasterDevice>
        }
        interface ActiveParticipationInvitation$Companion { serializer(): any }

        class AssignedMasterDevice
        {
            constructor( device: any, registration: DeviceRegistration | null )

            static get Companion(): AssignedMasterDevice$Companion

            readonly device: any
            readonly registration: DeviceRegistration | null
        }
        interface AssignedMasterDevice$Companion { serializer(): any }

        class Participation
        {
            constructor( studyDeploymentId: UUID, participantId?: UUID )

            static get Companion(): Participation$Companion

            readonly studyDeploymentId: UUID
            readonly participantId: UUID
        }
        interface Participation$Companion { serializer(): any }

        class ParticipantData
        {
            constructor( studyDeploymentId: UUID, data: HashMap<NamespacedId, any> )

            static get Companion(): ParticipantData$Companion

            readonly studyDeploymentId: UUID
            readonly data: HashMap<NamespacedId, any>
        }
        interface ParticipantData$Companion { serializer(): any }

        class ParticipantInvitation
        {
            constructor( participantId: UUID, assignedMasterDeviceRoleNames: HashSet<string>, identity: AccountIdentity, invitation: StudyInvitation )

            readonly participantId: UUID
            readonly assignedMasterDeviceRoleNames: HashSet<string>
            readonly identity: AccountIdentity
            readonly invitation: StudyInvitation

            static get Companion(): ParticipantInvitation$Companion
        }
        interface ParticipantInvitation$Companion { serializer(): any }

        class StudyInvitation
        {
            constructor( name: string, description: string, applicationData?: string )

            static get Companion(): StudyInvitation$Companion

            readonly name: string
            readonly description: string
            readonly applicationData: string
        }
        interface StudyInvitation$Companion
        {
            serializer(): any;
            empty(): StudyInvitation;
        }
    }


    namespace dk.cachet.carp.deployments.infrastructure
    {
        import ParticipantInvitation = dk.cachet.carp.deployments.application.users.ParticipantInvitation
        import StudyInvitation = dk.cachet.carp.deployments.application.users.StudyInvitation


        abstract class DeploymentServiceRequest
        {
            static get Companion(): DeploymentServiceRequest$Companion
        }
        interface DeploymentServiceRequest$Companion { serializer(): any }

        namespace DeploymentServiceRequest
        {
            class CreateStudyDeployment extends DeploymentServiceRequest
            {
                constructor( protocol: StudyProtocolSnapshot, invitations: ArrayList<ParticipantInvitation>, connectedDevicePreregistrations?: HashMap<String, DeviceRegistration> )
            }
            class GetStudyDeploymentStatus extends DeploymentServiceRequest
            {
                constructor( studyDeploymentId: UUID )
            }
            class GetStudyDeploymentStatusList extends DeploymentServiceRequest
            {
                constructor( studyDeploymentIds: HashSet<UUID> )
            }
            class RegisterDevice extends DeploymentServiceRequest
            {
                constructor( studyDeploymentId: UUID, deviceRoleName: string, registration: DeviceRegistration )
            }
            class UnregisterDevice extends DeploymentServiceRequest
            {
                constructor( studyDeploymentId: UUID, deviceRoleName: string )
            }
            class GetDeviceDeploymentFor extends DeploymentServiceRequest
            {
                constructor( studyDeploymentId: UUID, masterDeviceRoleName: string )
            }
            class DeploymentSuccessful extends DeploymentServiceRequest
            {
                constructor( studyDeploymentId: UUID, masterDeviceRoleName: string, deviceDeploymentLastUpdateDate: DateTime )
            }
            class Stop extends DeploymentServiceRequest
            {
                constructor( studyDeploymentId: UUID )
            }
        }


        abstract class ParticipationServiceRequest
        {
            static get Companion(): ParticipationServiceRequest$Companion
        }
        interface ParticipationServiceRequest$Companion { serializer(): any }

        namespace ParticipationServiceRequest
        {
            class GetActiveParticipationInvitations extends ParticipationServiceRequest
            {
                constructor( accountId: UUID )
            }
            class GetParticipantData extends ParticipationServiceRequest
            {
                constructor( studyDeploymentId: UUID )
            }
            class GetParticipantDataList extends ParticipationServiceRequest
            {
                constructor( studyDeploymentIds: HashSet<UUID> )
            }
            class SetParticipantData extends ParticipationServiceRequest
            {
                constructor( studyDeploymentId: UUID, inputDataType: NamespacedId, data: any | null )
            }
        }
    }
}