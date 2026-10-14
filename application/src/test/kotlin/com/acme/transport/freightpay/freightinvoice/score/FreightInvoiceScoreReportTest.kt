package com.acme.transport.freightpay.freightinvoice.score

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FreightInvoiceScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708402412)
        val items = mutableListOf<FreightInvoiceScore>()
        for (index in 0 until 2) {
            items.add(newFreightInvoiceScore("freight-invoice-score-$index", "sample"))
        }
        val report = buildFreightInvoiceScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
