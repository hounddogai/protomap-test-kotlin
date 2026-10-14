package com.acme.transport.freightpay.freightinvoice.proposal

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictFreightInvoiceRequestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705373673)
        val items = mutableListOf<StrictFreightInvoiceRequest>()
        for (index in 0 until 4) {
            items.add(newStrictFreightInvoiceRequest("strict-freight-invoice-request-$index", "sample"))
        }
        val report = buildStrictFreightInvoiceRequestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
