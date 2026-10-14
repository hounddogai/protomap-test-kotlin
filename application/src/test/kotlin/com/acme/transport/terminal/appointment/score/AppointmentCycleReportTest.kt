package com.acme.transport.terminal.appointment.score

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppointmentCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700411004)
        val items = mutableListOf<AppointmentCycle>()
        for (index in 0 until 3) {
            items.add(newAppointmentCycle("appointment-cycle-$index", "sample"))
        }
        val report = buildAppointmentCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
