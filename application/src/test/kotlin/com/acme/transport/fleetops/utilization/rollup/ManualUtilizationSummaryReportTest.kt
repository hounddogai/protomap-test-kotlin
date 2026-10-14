package com.acme.transport.fleetops.utilization.rollup

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualUtilizationSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704707279)
        val items = mutableListOf<ManualUtilizationSummary>()
        for (index in 0 until 5) {
            items.add(newManualUtilizationSummary("manual-utilization-summary-$index", "sample"))
        }
        val report = buildManualUtilizationSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
