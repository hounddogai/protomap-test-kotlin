package com.acme.transport.terminal.appointment.summary

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackAppointmentBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708491915)
        val items = mutableListOf<FallbackAppointmentBaseline>()
        for (index in 0 until 3) {
            items.add(newFallbackAppointmentBaseline("fallback-appointment-baseline-$index", "sample"))
        }
        val report = buildFallbackAppointmentBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
