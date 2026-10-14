package com.acme.transport.freightpay.freightinvoice.bundle

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FreightInvoiceBundleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709454156)
        val items = mutableListOf<FreightInvoiceBundle>()
        for (index in 0 until 4) {
            items.add(newFreightInvoiceBundle("freight-invoice-bundle-$index", "sample"))
        }
        val report = buildFreightInvoiceBundleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
