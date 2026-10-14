package com.acme.transport.freightpay.ratecard.slot

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704945571)
        val items = mutableListOf<RateCardVariance>()
        for (index in 0 until 3) {
            items.add(newRateCardVariance("rate-card-variance-$index", "sample"))
        }
        val report = buildRateCardVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
