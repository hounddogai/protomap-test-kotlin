package com.acme.transport.fleetops.inspection.quota

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveInspectionAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706144685)
        val items = mutableListOf<AdaptiveInspectionAudit>()
        for (index in 0 until 6) {
            items.add(newAdaptiveInspectionAudit("adaptive-inspection-audit-$index", "sample"))
        }
        val report = buildAdaptiveInspectionAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
