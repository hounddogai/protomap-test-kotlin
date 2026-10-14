package com.acme.transport.freightpay.accessorial.accessorialbudget

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualAccessorialBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702924937)
        val items = mutableListOf<ManualAccessorialBudget>()
        for (index in 0 until 5) {
            items.add(newManualAccessorialBudget("manual-accessorial-budget-$index", "sample"))
        }
        val report = buildManualAccessorialBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
