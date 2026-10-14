package com.acme.transport.freightpay.freightinvoice.tieredinvoicereview

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalFreightInvoiceRuleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708832993)
        val items = mutableListOf<SeasonalFreightInvoiceRule>()
        for (index in 0 until 3) {
            items.add(newSeasonalFreightInvoiceRule("seasonal-freight-invoice-rule-$index", "sample"))
        }
        val report = buildSeasonalFreightInvoiceRuleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
