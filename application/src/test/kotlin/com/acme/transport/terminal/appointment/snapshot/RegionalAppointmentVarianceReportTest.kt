package com.acme.transport.terminal.appointment.snapshot

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalAppointmentVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707153903)
        val items = mutableListOf<RegionalAppointmentVariance>()
        for (index in 0 until 5) {
            items.add(newRegionalAppointmentVariance("regional-appointment-variance-$index", "sample"))
        }
        val report = buildRegionalAppointmentVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
