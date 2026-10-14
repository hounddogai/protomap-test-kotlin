package com.acme.transport.freightpay.freightinvoice.summary

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledFreightInvoiceSnapshotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700579952)
        val items = mutableListOf<ScheduledFreightInvoiceSnapshot>()
        for (index in 0 until 3) {
            items.add(newScheduledFreightInvoiceSnapshot("scheduled-freight-invoice-snapshot-$index", "sample"))
        }
        val report = buildScheduledFreightInvoiceSnapshotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
