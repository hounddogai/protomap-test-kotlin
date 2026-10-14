package com.acme.transport.freightpay.freightinvoice.baseline

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingFreightInvoiceBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709658027)
        val items = mutableListOf<RollingFreightInvoiceBaseline>()
        for (index in 0 until 4) {
            items.add(newRollingFreightInvoiceBaseline("rolling-freight-invoice-baseline-$index", "sample"))
        }
        val report = buildRollingFreightInvoiceBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
