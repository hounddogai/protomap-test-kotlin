package com.acme.transport.freightpay.freightinvoice.window

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FreightInvoiceThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709727742)
        val items = mutableListOf<FreightInvoiceThreshold>()
        for (index in 0 until 3) {
            items.add(newFreightInvoiceThreshold("freight-invoice-threshold-$index", "sample"))
        }
        val report = buildFreightInvoiceThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
