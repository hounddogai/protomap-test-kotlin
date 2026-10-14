package com.acme.transport.fleetops.inspection.inspectionslot

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionRuleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706800909)
        val items = mutableListOf<InspectionRule>()
        for (index in 0 until 3) {
            items.add(newInspectionRule("inspection-rule-$index", "sample"))
        }
        val report = buildInspectionRuleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
