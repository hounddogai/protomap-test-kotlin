package com.acme.transport.freightpay.freightinvoice.baseline

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalFreightInvoiceWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700229163)
        val items = mutableListOf<SeasonalFreightInvoiceWindow>()
        for (index in 0 until 6) {
            items.add(newSeasonalFreightInvoiceWindow("seasonal-freight-invoice-window-$index", "sample"))
        }
        val report = buildSeasonalFreightInvoiceWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
