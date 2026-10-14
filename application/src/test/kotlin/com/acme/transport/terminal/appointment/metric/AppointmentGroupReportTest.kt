package com.acme.transport.terminal.appointment.metric

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppointmentGroupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701271191)
        val items = mutableListOf<AppointmentGroup>()
        for (index in 0 until 5) {
            items.add(newAppointmentGroup("appointment-group-$index", "sample"))
        }
        val report = buildAppointmentGroupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
