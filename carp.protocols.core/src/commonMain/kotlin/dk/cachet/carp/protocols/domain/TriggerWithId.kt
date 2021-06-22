package dk.cachet.carp.protocols.domain

import dk.cachet.carp.common.application.devices.AnyDeviceDescriptor
import dk.cachet.carp.common.application.tasks.Measure
import dk.cachet.carp.common.application.tasks.TaskDescriptor
import dk.cachet.carp.common.application.triggers.Trigger


/**
 * A [trigger] with an [id] as assigned to in a [StudyProtocol].
 */
data class TriggerWithId( val id: Int, val trigger: Trigger )
{
    /**
     * Indicate that a task should measure the conditions under which this [trigger] started or stopped it.
     */
    fun measure(): Measure.TriggerData = Measure.TriggerData( id )
}


/**
 * Retrieve the [Trigger] with its assigned ID ([TriggerWithId]) in the specified study [protocol].
 *
 * @throws IllegalArgumentException when the trigger is not part of the passed study protocol.
 */
fun Trigger.within( protocol: StudyProtocol ): TriggerWithId =
    protocol.triggers.firstOrNull { it.trigger == this }
        ?: throw IllegalArgumentException( "This trigger is not part of the passed study protocol." )

/**
 * Specify that a [task] should start on the specified [destinationDevice] once this [Trigger] initiates.
 */
fun TriggerWithId.start( task: TaskDescriptor, destinationDevice: AnyDeviceDescriptor ) =
    this.trigger.start( task, destinationDevice )

/**
 * Specify that a [task] should stop on the specified [destinationDevice] once this [Trigger] initiates.
 */
fun TriggerWithId.stop( task: TaskDescriptor, destinationDevice: AnyDeviceDescriptor ) =
    this.trigger.stop( task, destinationDevice )
