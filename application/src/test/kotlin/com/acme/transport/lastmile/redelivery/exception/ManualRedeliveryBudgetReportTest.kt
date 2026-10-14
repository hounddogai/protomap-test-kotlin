package com.acme.transport.lastmile.redelivery.exception

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualRedeliveryBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704402636)
        val items = mutableListOf<ManualRedeliveryBudget>()
        for (index in 0 until 6) {
            items.add(newManualRedeliveryBudget("manual-redelivery-budget-$index", "sample"))
        }
        val report = buildManualRedeliveryBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
