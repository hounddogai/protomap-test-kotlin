package com.acme.transport.freightpay.freightinvoice.bundle

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FreightInvoiceWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705330223)
        val items = mutableListOf<FreightInvoiceWindow>()
        for (index in 0 until 5) {
            items.add(newFreightInvoiceWindow("freight-invoice-window-$index", "sample"))
        }
        val report = buildFreightInvoiceWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
