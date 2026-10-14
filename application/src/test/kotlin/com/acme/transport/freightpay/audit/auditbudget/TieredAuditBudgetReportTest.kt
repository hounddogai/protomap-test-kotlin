package com.acme.transport.freightpay.audit.auditbudget

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredAuditBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702238173)
        val items = mutableListOf<TieredAuditBudget>()
        for (index in 0 until 2) {
            items.add(newTieredAuditBudget("tiered-audit-budget-$index", "sample"))
        }
        val report = buildTieredAuditBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
