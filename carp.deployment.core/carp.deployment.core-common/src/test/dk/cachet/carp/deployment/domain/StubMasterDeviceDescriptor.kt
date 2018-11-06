package dk.cachet.carp.deployment.domain

import dk.cachet.carp.common.Trilean
import dk.cachet.carp.common.serialization.PolymorphicSerializer
import dk.cachet.carp.protocols.domain.devices.*
import kotlinx.serialization.Serializable


@Serializable
data class StubMasterDeviceDescriptor( override val roleName: String = "Stub master device" ) : MasterDeviceDescriptor()
{
    companion object
    {
        init { PolymorphicSerializer.registerSerializer( StubMasterDeviceDescriptor::class, "dk.cachet.carp.deployment.domain.StubMasterDeviceDescriptor" ) }
    }

    override fun isValidConfiguration( configuration: DeviceRegistration ) = Trilean.TRUE
}