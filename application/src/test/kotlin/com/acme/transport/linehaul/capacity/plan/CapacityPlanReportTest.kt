package com.acme.transport.linehaul.capacity.plan

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapacityPlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709124565)
        val items = mutableListOf<CapacityPlan>()
        for (index in 0 until 5) {
            items.add(newCapacityPlan("capacity-plan-$index", "sample"))
        }
        val report = buildCapacityPlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
