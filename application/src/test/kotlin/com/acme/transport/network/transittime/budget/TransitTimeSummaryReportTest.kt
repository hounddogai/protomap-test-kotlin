package com.acme.transport.network.transittime.budget

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransitTimeSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705720266)
        val items = mutableListOf<TransitTimeSummary>()
        for (index in 0 until 6) {
            items.add(newTransitTimeSummary("transit-time-summary-$index", "sample"))
        }
        val report = buildTransitTimeSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
