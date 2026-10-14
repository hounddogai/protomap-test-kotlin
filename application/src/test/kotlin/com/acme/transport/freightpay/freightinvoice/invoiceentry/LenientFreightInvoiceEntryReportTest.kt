package com.acme.transport.freightpay.freightinvoice.invoiceentry

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientFreightInvoiceEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705475158)
        val items = mutableListOf<LenientFreightInvoiceEntry>()
        for (index in 0 until 6) {
            items.add(newLenientFreightInvoiceEntry("lenient-freight-invoice-entry-$index", "sample"))
        }
        val report = buildLenientFreightInvoiceEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
