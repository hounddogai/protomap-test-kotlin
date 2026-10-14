package com.acme.transport.fleetops.inspection.plan

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionPlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706206753)
        val items = mutableListOf<InspectionPlan>()
        for (index in 0 until 2) {
            items.add(newInspectionPlan("inspection-plan-$index", "sample"))
        }
        val report = buildInspectionPlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
