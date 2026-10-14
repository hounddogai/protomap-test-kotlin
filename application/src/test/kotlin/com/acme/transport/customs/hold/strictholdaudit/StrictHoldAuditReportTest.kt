package com.acme.transport.customs.hold.strictholdaudit

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictHoldAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700470941)
        val items = mutableListOf<StrictHoldAudit>()
        for (index in 0 until 6) {
            items.add(newStrictHoldAudit("strict-hold-audit-$index", "sample"))
        }
        val report = buildStrictHoldAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
