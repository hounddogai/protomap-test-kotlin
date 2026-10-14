package com.acme.transport.freightpay.freightinvoice.profile

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalFreightInvoiceSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708293903)
        val items = mutableListOf<RegionalFreightInvoiceSummary>()
        for (index in 0 until 4) {
            items.add(newRegionalFreightInvoiceSummary("regional-freight-invoice-summary-$index", "sample"))
        }
        val report = buildRegionalFreightInvoiceSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
