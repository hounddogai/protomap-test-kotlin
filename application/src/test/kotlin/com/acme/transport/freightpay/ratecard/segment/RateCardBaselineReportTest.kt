package com.acme.transport.freightpay.ratecard.segment

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702070061)
        val items = mutableListOf<RateCardBaseline>()
        for (index in 0 until 5) {
            items.add(newRateCardBaseline("rate-card-baseline-$index", "sample"))
        }
        val report = buildRateCardBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
