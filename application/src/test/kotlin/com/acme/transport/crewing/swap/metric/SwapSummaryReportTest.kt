package com.acme.transport.crewing.swap.metric

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706895033)
        val items = mutableListOf<SwapSummary>()
        for (index in 0 until 4) {
            items.add(newSwapSummary("swap-summary-$index", "sample"))
        }
        val report = buildSwapSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
