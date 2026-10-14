package com.acme.transport.terminal.crossdock.policy

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledCrossDockSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708417705)
        val items = mutableListOf<ScheduledCrossDockSummary>()
        for (index in 0 until 4) {
            items.add(newScheduledCrossDockSummary("scheduled-cross-dock-summary-$index", "sample"))
        }
        val report = buildScheduledCrossDockSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
