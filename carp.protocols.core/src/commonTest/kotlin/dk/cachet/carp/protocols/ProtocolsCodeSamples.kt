package dk.cachet.carp.protocols

import dk.cachet.carp.common.application.RecurrenceRule
import dk.cachet.carp.common.application.TimeOfDay
import dk.cachet.carp.common.application.devices.CustomProtocolDevice
import dk.cachet.carp.common.application.devices.Smartphone
import dk.cachet.carp.common.application.sampling.Granularity
import dk.cachet.carp.common.application.tasks.BackgroundTask
import dk.cachet.carp.common.application.tasks.CustomProtocolTask
import dk.cachet.carp.common.application.triggers.ScheduledTrigger
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.protocols.domain.ProtocolOwner
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.domain.start
import dk.cachet.carp.protocols.domain.within
import dk.cachet.carp.test.runSuspendTest
import kotlinx.serialization.encodeToString
import kotlin.test.*


class ProtocolsCodeSamples
{
    @Test
    @Suppress( "UnusedPrivateMember", "UNUSED_VARIABLE" )
    fun readme() = runSuspendTest {
        // Create a new study protocol.
        val owner = ProtocolOwner()
        val protocol = StudyProtocol( owner, "Track patient movement" )

        // Define which devices are used for data collection.
        val phone = Smartphone( "Patient's phone" )
        {
            // Configure device-specific options, e.g., frequency to collect data at.
            defaultSamplingConfiguration {
                geolocation { batteryNormal { granularity = Granularity.Balanced } }
            }
        }
        protocol.addMasterDevice( phone )

        // Define what needs to be measured, on which device, when.
        val sensors = Smartphone.Sensors
        val trackMovement = Smartphone.Tasks.BACKGROUND.create( "Track movement" ) {
            measures = listOf( sensors.GEOLOCATION.measure(), sensors.STEP_COUNT.measure() )
            description = "Track activity level and number of places visited per day."
        }
        protocol.addTaskControl( phone.atStartOfStudy().start( trackMovement, phone ) )

        // JSON output of the study protocol, compatible with the rest of the CARP infrastructure.
        val json: String = JSON.encodeToString( protocol.getSnapshot() )
    }

    @Test
    fun custom_protocol() = runSuspendTest {
        val owner = ProtocolOwner()
        val protocol = StudyProtocol( owner, "Study for CAMS runtime" )

        val phone = CustomProtocolDevice( "CAMS smartphone" )
        protocol.addMasterDevice( phone )

        val camsProtocol = """{ "custom": "configuration" }""" // Anything which can be serialized to a string.
        val camsTask = CustomProtocolTask( "Monitor diabetes", camsProtocol )
        protocol.addTaskControl( phone.atStartOfStudy().start( camsTask, phone ) )

        val json: String = JSON.encodeToString( protocol.getSnapshot() )
        assertTrue( json.isNotEmpty() )
    }

    @Test
    fun measure_trigger_data() = runSuspendTest {
        val owner = ProtocolOwner()
        val protocol = StudyProtocol( owner, "Ping every noon" )
        val phone = Smartphone( "Ping source" )
        val daily = ScheduledTrigger( phone, TimeOfDay( 12, 0, 0 ), RecurrenceRule.daily( 1 ) )
        protocol.addMasterDevice( phone )
        protocol.addTrigger( daily ) // Trigger needs to be added before measure for it can be initialized.

        val measurePing = daily.within( protocol ).measure()
        val ping = BackgroundTask("Ping", listOf( measurePing ) )
        protocol.addTaskControl( daily.start( ping, phone ) )
    }
}
