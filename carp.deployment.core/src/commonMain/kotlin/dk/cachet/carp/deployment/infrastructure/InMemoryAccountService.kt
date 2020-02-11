package dk.cachet.carp.deployment.infrastructure

import dk.cachet.carp.common.users.Account
import dk.cachet.carp.common.users.AccountIdentity
import dk.cachet.carp.deployment.domain.users.AccountService
import dk.cachet.carp.deployment.domain.users.Participation
import dk.cachet.carp.deployment.domain.users.StudyInvitation


/**
 * An [AccountService] which holds accounts in memory as long as the instance is held in memory.
 */
class InMemoryAccountService : AccountService
{
    private val accounts: MutableList<Account> = mutableListOf()


    /**
     * Create a new account identified by [identity] to participate in a study deployment identified by [participation].
     * The [invitation] and account details should be delivered, or made available, to the user managing the [identity].
     *
     * @throws IllegalArgumentException when an account with a matching [AccountIdentity] already exists.
     */
    override suspend fun inviteNewAccount( identity: AccountIdentity, invitation: StudyInvitation, participation: Participation ): Account
    {
        require( accounts.none { it.identity == identity } )

        val account = Account( identity )
        accounts.add( account )

        return account
    }

    /**
     * Deliver an [invitation] to participate in a study deployment identified by [participation], or make it available, to the user managing [identity].
     *
     * @throws IllegalArgumentException when no account with a matching [identity] exists.
     */
    override suspend fun inviteExistingAccount( identity: AccountIdentity, invitation: StudyInvitation, participation: Participation )
    {
        require( accounts.any { it.identity == identity } )
    }

    /**
     * Returns the [Account] which has the specified [identity], or null when no account is found.
     */
    override suspend fun findAccount( identity: AccountIdentity ): Account? =
        accounts.firstOrNull { it.identity == identity }
}
