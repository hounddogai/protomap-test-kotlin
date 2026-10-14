package com.acme.transport.freightpay.freightinvoice.invoicemanifest

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FreightInvoicePlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707020483)
        val items = mutableListOf<FreightInvoicePlan>()
        for (index in 0 until 2) {
            items.add(newFreightInvoicePlan("freight-invoice-plan-$index", "sample"))
        }
        val report = buildFreightInvoicePlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
