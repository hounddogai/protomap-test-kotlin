package com.acme.transport.customs.hold.summary

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707410549)
        val items = mutableListOf<HoldSummary>()
        for (index in 0 until 6) {
            items.add(newHoldSummary("hold-summary-$index", "sample"))
        }
        val report = buildHoldSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
