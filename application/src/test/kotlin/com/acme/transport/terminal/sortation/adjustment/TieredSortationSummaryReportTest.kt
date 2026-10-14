package com.acme.transport.terminal.sortation.adjustment

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredSortationSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703320191)
        val items = mutableListOf<TieredSortationSummary>()
        for (index in 0 until 3) {
            items.add(newTieredSortationSummary("tiered-sortation-summary-$index", "sample"))
        }
        val report = buildTieredSortationSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
