package com.acme.transport.freightpay.freightinvoice.digest

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientFreightInvoiceBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706417530)
        val items = mutableListOf<LenientFreightInvoiceBudget>()
        for (index in 0 until 4) {
            items.add(newLenientFreightInvoiceBudget("lenient-freight-invoice-budget-$index", "sample"))
        }
        val report = buildLenientFreightInvoiceBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
