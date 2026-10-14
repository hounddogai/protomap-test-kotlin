package com.acme.transport.safety.violation.primaryviolationquota

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedViolationAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708458449)
        val items = mutableListOf<CappedViolationAudit>()
        for (index in 0 until 2) {
            items.add(newCappedViolationAudit("capped-violation-audit-$index", "sample"))
        }
        val report = buildCappedViolationAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
