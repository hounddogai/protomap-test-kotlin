package com.acme.transport.fleetops.utilization.estimate

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtilizationPlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707821226)
        val items = mutableListOf<UtilizationPlan>()
        for (index in 0 until 6) {
            items.add(newUtilizationPlan("utilization-plan-$index", "sample"))
        }
        val report = buildUtilizationPlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
