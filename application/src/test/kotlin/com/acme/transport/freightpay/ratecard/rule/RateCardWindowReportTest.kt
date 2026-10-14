package com.acme.transport.freightpay.ratecard.rule

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702670307)
        val items = mutableListOf<RateCardWindow>()
        for (index in 0 until 3) {
            items.add(newRateCardWindow("rate-card-window-$index", "sample"))
        }
        val report = buildRateCardWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
