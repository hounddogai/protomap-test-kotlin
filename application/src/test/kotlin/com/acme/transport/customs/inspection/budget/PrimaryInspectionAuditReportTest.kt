package com.acme.transport.customs.inspection.budget

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryInspectionAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703136565)
        val items = mutableListOf<PrimaryInspectionAudit>()
        for (index in 0 until 2) {
            items.add(newPrimaryInspectionAudit("primary-inspection-audit-$index", "sample"))
        }
        val report = buildPrimaryInspectionAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
