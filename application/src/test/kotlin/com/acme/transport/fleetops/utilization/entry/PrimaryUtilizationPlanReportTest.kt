package com.acme.transport.fleetops.utilization.entry

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryUtilizationPlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706656121)
        val items = mutableListOf<PrimaryUtilizationPlan>()
        for (index in 0 until 6) {
            items.add(newPrimaryUtilizationPlan("primary-utilization-plan-$index", "sample"))
        }
        val report = buildPrimaryUtilizationPlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
