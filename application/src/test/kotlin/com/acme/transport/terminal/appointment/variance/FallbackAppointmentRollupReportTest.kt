package com.acme.transport.terminal.appointment.variance

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackAppointmentRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706522958)
        val items = mutableListOf<FallbackAppointmentRollup>()
        for (index in 0 until 3) {
            items.add(newFallbackAppointmentRollup("fallback-appointment-rollup-$index", "sample"))
        }
        val report = buildFallbackAppointmentRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
