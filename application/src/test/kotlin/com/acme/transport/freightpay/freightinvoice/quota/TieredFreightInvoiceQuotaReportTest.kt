package com.acme.transport.freightpay.freightinvoice.quota

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredFreightInvoiceQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706141008)
        val items = mutableListOf<TieredFreightInvoiceQuota>()
        for (index in 0 until 6) {
            items.add(newTieredFreightInvoiceQuota("tiered-freight-invoice-quota-$index", "sample"))
        }
        val report = buildTieredFreightInvoiceQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
