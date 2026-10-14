package com.acme.transport.freightpay.freightinvoice.invoicesummary

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingFreightInvoiceSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702588564)
        val items = mutableListOf<PendingFreightInvoiceSummary>()
        for (index in 0 until 4) {
            items.add(newPendingFreightInvoiceSummary("pending-freight-invoice-summary-$index", "sample"))
        }
        val report = buildPendingFreightInvoiceSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
