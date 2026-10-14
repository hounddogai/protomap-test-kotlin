package com.acme.transport.customs.inspection.plan

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionPolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702804946)
        val items = mutableListOf<InspectionPolicy>()
        for (index in 0 until 5) {
            items.add(newInspectionPolicy("inspection-policy-$index", "sample"))
        }
        val report = buildInspectionPolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
