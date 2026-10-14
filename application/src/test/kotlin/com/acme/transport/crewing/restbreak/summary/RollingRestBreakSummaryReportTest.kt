package com.acme.transport.crewing.restbreak.summary

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingRestBreakSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706361784)
        val items = mutableListOf<RollingRestBreakSummary>()
        for (index in 0 until 3) {
            items.add(newRollingRestBreakSummary("rolling-rest-break-summary-$index", "sample"))
        }
        val report = buildRollingRestBreakSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
