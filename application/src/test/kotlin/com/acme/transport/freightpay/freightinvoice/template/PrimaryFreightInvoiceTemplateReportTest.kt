package com.acme.transport.freightpay.freightinvoice.template

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryFreightInvoiceTemplateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708786754)
        val items = mutableListOf<PrimaryFreightInvoiceTemplate>()
        for (index in 0 until 2) {
            items.add(newPrimaryFreightInvoiceTemplate("primary-freight-invoice-template-$index", "sample"))
        }
        val report = buildPrimaryFreightInvoiceTemplateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
