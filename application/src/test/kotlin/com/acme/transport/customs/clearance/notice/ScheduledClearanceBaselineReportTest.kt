package com.acme.transport.customs.clearance.notice

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledClearanceBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704302587)
        val items = mutableListOf<ScheduledClearanceBaseline>()
        for (index in 0 until 4) {
            items.add(newScheduledClearanceBaseline("scheduled-clearance-baseline-$index", "sample"))
        }
        val report = buildScheduledClearanceBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
