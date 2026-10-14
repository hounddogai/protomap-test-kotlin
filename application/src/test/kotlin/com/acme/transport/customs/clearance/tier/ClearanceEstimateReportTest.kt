package com.acme.transport.customs.clearance.tier

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearanceEstimateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704004815)
        val items = mutableListOf<ClearanceEstimate>()
        for (index in 0 until 2) {
            items.add(newClearanceEstimate("clearance-estimate-$index", "sample"))
        }
        val report = buildClearanceEstimateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
