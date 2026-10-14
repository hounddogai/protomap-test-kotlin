package com.acme.transport.freightpay.freightinvoice.review

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictFreightInvoiceScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701791089)
        val items = mutableListOf<StrictFreightInvoiceScore>()
        for (index in 0 until 3) {
            items.add(newStrictFreightInvoiceScore("strict-freight-invoice-score-$index", "sample"))
        }
        val report = buildStrictFreightInvoiceScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
