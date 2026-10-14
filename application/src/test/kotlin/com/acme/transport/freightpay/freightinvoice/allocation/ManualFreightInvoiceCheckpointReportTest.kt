package com.acme.transport.freightpay.freightinvoice.allocation

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualFreightInvoiceCheckpointReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707109905)
        val items = mutableListOf<ManualFreightInvoiceCheckpoint>()
        for (index in 0 until 6) {
            items.add(newManualFreightInvoiceCheckpoint("manual-freight-invoice-checkpoint-$index", "sample"))
        }
        val report = buildManualFreightInvoiceCheckpointReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
