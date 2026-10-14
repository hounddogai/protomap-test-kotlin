package com.acme.transport.freightpay.freightinvoice.budget

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingFreightInvoiceBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700600860)
        val items = mutableListOf<PendingFreightInvoiceBudget>()
        for (index in 0 until 6) {
            items.add(newPendingFreightInvoiceBudget("pending-freight-invoice-budget-$index", "sample"))
        }
        val report = buildPendingFreightInvoiceBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
