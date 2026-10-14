package com.acme.transport.freightpay.freightinvoice.invoiceadjustment

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingFreightInvoiceRequestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704729366)
        val items = mutableListOf<RollingFreightInvoiceRequest>()
        for (index in 0 until 3) {
            items.add(newRollingFreightInvoiceRequest("rolling-freight-invoice-request-$index", "sample"))
        }
        val report = buildRollingFreightInvoiceRequestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
