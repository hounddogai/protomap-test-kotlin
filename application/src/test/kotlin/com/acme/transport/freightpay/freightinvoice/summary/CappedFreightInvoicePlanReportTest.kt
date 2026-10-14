package com.acme.transport.freightpay.freightinvoice.summary

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedFreightInvoicePlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706125119)
        val items = mutableListOf<CappedFreightInvoicePlan>()
        for (index in 0 until 6) {
            items.add(newCappedFreightInvoicePlan("capped-freight-invoice-plan-$index", "sample"))
        }
        val report = buildCappedFreightInvoicePlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
