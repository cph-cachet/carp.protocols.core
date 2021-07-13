package dk.cachet.carp.common.infrastructure.services

import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.EventBus
import dk.cachet.carp.common.application.services.IntegrationEvent
import kotlin.reflect.KClass


/**
 * A base implementation of [EventBus] which sets up a subscription channel per application service
 * from which events originate.
 *
 * The [EventBus] abstraction does not assume a one-to-one mapping between a message queue channel
 * and application service. E.g., you could implement two channels per application service, or use one for two services.
 * But, a one-to-one mapping is a reasonable convention. This base class implements that convention.
 */
abstract class ChannelConventionEventBus : EventBus()
{
    final override suspend fun <
        TService : ApplicationService<TService, TEvent>,
        TEvent : IntegrationEvent<TService>
    > publish( publishingService: KClass<TService>, event: TEvent ) =
        publishToChannel( publishingService, event )

    override fun activateHandlers( subscriber: Any, handlers: List<Handler> )
    {
        handlers
            .groupBy { it.eventSource }
            .forEach { (source, _) -> subscribeToChannel( source, ::redirectEvent ) }
    }

    private suspend fun redirectEvent( event: IntegrationEvent<*> )
    {
        // Find all active handlers listening to the published event type.
        val handlers = subscribers.values
            .filter { it.isActivated }
            .flatMap { it.eventHandlers.filter { handler -> handler.eventType.isInstance( event ) } }

        // Publish.
        handlers.forEach { it.handler( event ) }
    }

    /**
     * Publish the specified [event] to the channel identified by [channelIdentifier].
     */
    abstract suspend fun <
        TService : ApplicationService<TService, TEvent>,
        TEvent : IntegrationEvent<TService>
    > publishToChannel( channelIdentifier: KClass<TService>, event: TEvent )

    /**
     * Forward all events of the channel identified by [channelIdentifier] to [handler].
     */
    abstract fun subscribeToChannel( channelIdentifier: KClass<*>, handler: suspend (IntegrationEvent<*>) -> Unit )
}
