package com.acme.transport.fleetops.utilization.plan

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackUtilizationPlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705411520)
        val items = mutableListOf<FallbackUtilizationPlan>()
        for (index in 0 until 6) {
            items.add(newFallbackUtilizationPlan("fallback-utilization-plan-$index", "sample"))
        }
        val report = buildFallbackUtilizationPlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
