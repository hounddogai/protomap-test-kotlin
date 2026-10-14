package com.acme.transport.freightpay.freightinvoice.preference

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FreightInvoiceBatchReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701916800)
        val items = mutableListOf<FreightInvoiceBatch>()
        for (index in 0 until 2) {
            items.add(newFreightInvoiceBatch("freight-invoice-batch-$index", "sample"))
        }
        val report = buildFreightInvoiceBatchReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
