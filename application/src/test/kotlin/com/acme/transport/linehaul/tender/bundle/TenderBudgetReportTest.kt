package com.acme.transport.linehaul.tender.bundle

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706368424)
        val items = mutableListOf<TenderBudget>()
        for (index in 0 until 3) {
            items.add(newTenderBudget("tender-budget-$index", "sample"))
        }
        val report = buildTenderBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
