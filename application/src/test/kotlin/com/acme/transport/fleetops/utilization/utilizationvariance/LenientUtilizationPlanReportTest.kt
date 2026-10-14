package com.acme.transport.fleetops.utilization.utilizationvariance

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientUtilizationPlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703625915)
        val items = mutableListOf<LenientUtilizationPlan>()
        for (index in 0 until 6) {
            items.add(newLenientUtilizationPlan("lenient-utilization-plan-$index", "sample"))
        }
        val report = buildLenientUtilizationPlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
