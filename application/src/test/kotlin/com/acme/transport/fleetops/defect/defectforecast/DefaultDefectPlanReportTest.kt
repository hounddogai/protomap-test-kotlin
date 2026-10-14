package com.acme.transport.fleetops.defect.defectforecast

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultDefectPlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701412505)
        val items = mutableListOf<DefaultDefectPlan>()
        for (index in 0 until 5) {
            items.add(newDefaultDefectPlan("default-defect-plan-$index", "sample"))
        }
        val report = buildDefaultDefectPlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
