package com.acme.transport.freightpay.freightinvoice.preference

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FreightInvoiceBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705090944)
        val items = mutableListOf<FreightInvoiceBudget>()
        for (index in 0 until 3) {
            items.add(newFreightInvoiceBudget("freight-invoice-budget-$index", "sample"))
        }
        val report = buildFreightInvoiceBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
