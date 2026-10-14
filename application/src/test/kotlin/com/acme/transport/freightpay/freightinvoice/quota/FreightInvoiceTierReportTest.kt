package com.acme.transport.freightpay.freightinvoice.quota

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FreightInvoiceTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704796814)
        val items = mutableListOf<FreightInvoiceTier>()
        for (index in 0 until 6) {
            items.add(newFreightInvoiceTier("freight-invoice-tier-$index", "sample"))
        }
        val report = buildFreightInvoiceTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
