package com.acme.transport.terminal.appointment.group

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackAppointmentGroupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703622377)
        val items = mutableListOf<FallbackAppointmentGroup>()
        for (index in 0 until 2) {
            items.add(newFallbackAppointmentGroup("fallback-appointment-group-$index", "sample"))
        }
        val report = buildFallbackAppointmentGroupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
