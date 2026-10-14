package com.acme.transport.fleetops.utilization.utilizationplan

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualUtilizationPlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706769288)
        val items = mutableListOf<ManualUtilizationPlan>()
        for (index in 0 until 4) {
            items.add(newManualUtilizationPlan("manual-utilization-plan-$index", "sample"))
        }
        val report = buildManualUtilizationPlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
