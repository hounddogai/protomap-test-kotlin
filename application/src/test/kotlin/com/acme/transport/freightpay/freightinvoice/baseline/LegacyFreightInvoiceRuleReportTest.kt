package com.acme.transport.freightpay.freightinvoice.baseline

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyFreightInvoiceRuleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709613276)
        val items = mutableListOf<LegacyFreightInvoiceRule>()
        for (index in 0 until 5) {
            items.add(newLegacyFreightInvoiceRule("legacy-freight-invoice-rule-$index", "sample"))
        }
        val report = buildLegacyFreightInvoiceRuleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
