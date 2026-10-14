package com.acme.transport.fleetops.utilization.utilizationestimate

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalUtilizationEstimateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704380821)
        val items = mutableListOf<GlobalUtilizationEstimate>()
        for (index in 0 until 2) {
            items.add(newGlobalUtilizationEstimate("global-utilization-estimate-$index", "sample"))
        }
        val report = buildGlobalUtilizationEstimateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
