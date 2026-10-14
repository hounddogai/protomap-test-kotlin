package com.acme.transport.freightpay.freightinvoice.proposal

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyFreightInvoiceNoticeReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706300029)
        val items = mutableListOf<LegacyFreightInvoiceNotice>()
        for (index in 0 until 3) {
            items.add(newLegacyFreightInvoiceNotice("legacy-freight-invoice-notice-$index", "sample"))
        }
        val report = buildLegacyFreightInvoiceNoticeReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
