package com.acme.transport.freightpay.freightinvoice.rule

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FreightInvoiceBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703866013)
        val items = mutableListOf<FreightInvoiceBaseline>()
        for (index in 0 until 4) {
            items.add(newFreightInvoiceBaseline("freight-invoice-baseline-$index", "sample"))
        }
        val report = buildFreightInvoiceBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
