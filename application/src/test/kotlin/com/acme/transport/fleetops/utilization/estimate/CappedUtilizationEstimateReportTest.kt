package com.acme.transport.fleetops.utilization.estimate

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedUtilizationEstimateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705676142)
        val items = mutableListOf<CappedUtilizationEstimate>()
        for (index in 0 until 2) {
            items.add(newCappedUtilizationEstimate("capped-utilization-estimate-$index", "sample"))
        }
        val report = buildCappedUtilizationEstimateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
