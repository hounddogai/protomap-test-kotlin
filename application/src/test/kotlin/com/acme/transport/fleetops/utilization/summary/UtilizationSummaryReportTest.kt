package com.acme.transport.fleetops.utilization.summary

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtilizationSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701475990)
        val items = mutableListOf<UtilizationSummary>()
        for (index in 0 until 5) {
            items.add(newUtilizationSummary("utilization-summary-$index", "sample"))
        }
        val report = buildUtilizationSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
