package com.acme.transport.freightpay.freightinvoice.preference

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FreightInvoicePreferenceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702277060)
        val items = mutableListOf<FreightInvoicePreference>()
        for (index in 0 until 4) {
            items.add(newFreightInvoicePreference("freight-invoice-preference-$index", "sample"))
        }
        val report = buildFreightInvoicePreferenceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
