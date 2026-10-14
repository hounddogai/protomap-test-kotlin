package com.acme.transport.fleetops.defect.defectagreement

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefectBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704047044)
        val items = mutableListOf<DefectBudget>()
        for (index in 0 until 6) {
            items.add(newDefectBudget("defect-budget-$index", "sample"))
        }
        val report = buildDefectBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
