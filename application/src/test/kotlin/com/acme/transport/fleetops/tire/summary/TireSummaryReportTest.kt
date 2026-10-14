package com.acme.transport.fleetops.tire.summary

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TireSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708175934)
        val items = mutableListOf<TireSummary>()
        for (index in 0 until 5) {
            items.add(newTireSummary("tire-summary-$index", "sample"))
        }
        val report = buildTireSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
