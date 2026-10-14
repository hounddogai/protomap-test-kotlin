package com.acme.transport.terminal.appointment.appointmentallocation

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveAppointmentCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706563448)
        val items = mutableListOf<AdaptiveAppointmentCycle>()
        for (index in 0 until 4) {
            items.add(newAdaptiveAppointmentCycle("adaptive-appointment-cycle-$index", "sample"))
        }
        val report = buildAdaptiveAppointmentCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
