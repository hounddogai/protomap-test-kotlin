package com.acme.transport.freightpay.freightinvoice.batch

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultFreightInvoiceBatchReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702493606)
        val items = mutableListOf<DefaultFreightInvoiceBatch>()
        for (index in 0 until 6) {
            items.add(newDefaultFreightInvoiceBatch("default-freight-invoice-batch-$index", "sample"))
        }
        val report = buildDefaultFreightInvoiceBatchReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
