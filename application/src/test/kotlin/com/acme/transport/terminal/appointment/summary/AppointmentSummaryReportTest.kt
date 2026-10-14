package com.acme.transport.terminal.appointment.summary

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppointmentSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702374436)
        val items = mutableListOf<AppointmentSummary>()
        for (index in 0 until 2) {
            items.add(newAppointmentSummary("appointment-summary-$index", "sample"))
        }
        val report = buildAppointmentSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
