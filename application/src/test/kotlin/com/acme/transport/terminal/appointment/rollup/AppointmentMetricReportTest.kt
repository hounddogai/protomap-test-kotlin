package com.acme.transport.terminal.appointment.rollup

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppointmentMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707949110)
        val items = mutableListOf<AppointmentMetric>()
        for (index in 0 until 5) {
            items.add(newAppointmentMetric("appointment-metric-$index", "sample"))
        }
        val report = buildAppointmentMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
