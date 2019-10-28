package dk.cachet.carp.studies.domain.users

import kotlinx.serialization.Serializable


/**
 * A unique name which identifies an [Account].
 */
@Serializable
data class Username( val name: String )