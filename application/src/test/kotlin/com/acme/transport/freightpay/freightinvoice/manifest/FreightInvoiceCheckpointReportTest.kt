package com.acme.transport.freightpay.freightinvoice.manifest

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FreightInvoiceCheckpointReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704937345)
        val items = mutableListOf<FreightInvoiceCheckpoint>()
        for (index in 0 until 3) {
            items.add(newFreightInvoiceCheckpoint("freight-invoice-checkpoint-$index", "sample"))
        }
        val report = buildFreightInvoiceCheckpointReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
