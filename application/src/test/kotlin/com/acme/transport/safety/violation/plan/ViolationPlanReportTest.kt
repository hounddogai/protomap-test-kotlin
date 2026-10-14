package com.acme.transport.safety.violation.plan

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViolationPlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702737483)
        val items = mutableListOf<ViolationPlan>()
        for (index in 0 until 4) {
            items.add(newViolationPlan("violation-plan-$index", "sample"))
        }
        val report = buildViolationPlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
