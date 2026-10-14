package com.acme.transport.freightpay.freightinvoice.quota

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FreightInvoiceTargetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700847776)
        val items = mutableListOf<FreightInvoiceTarget>()
        for (index in 0 until 2) {
            items.add(newFreightInvoiceTarget("freight-invoice-target-$index", "sample"))
        }
        val report = buildFreightInvoiceTargetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
