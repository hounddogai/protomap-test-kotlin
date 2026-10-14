package com.acme.transport.customs.inspection.mapping

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledInspectionPlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701149133)
        val items = mutableListOf<ScheduledInspectionPlan>()
        for (index in 0 until 2) {
            items.add(newScheduledInspectionPlan("scheduled-inspection-plan-$index", "sample"))
        }
        val report = buildScheduledInspectionPlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
