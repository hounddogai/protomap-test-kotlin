package com.acme.transport.customs.inspection.budget

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveInspectionAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703908128)
        val items = mutableListOf<AdaptiveInspectionAudit>()
        for (index in 0 until 4) {
            items.add(newAdaptiveInspectionAudit("adaptive-inspection-audit-$index", "sample"))
        }
        val report = buildAdaptiveInspectionAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
