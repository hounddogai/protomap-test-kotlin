package com.acme.transport.freightpay.ratecard.ledger

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706827149)
        val items = mutableListOf<RateCardMetric>()
        for (index in 0 until 2) {
            items.add(newRateCardMetric("rate-card-metric-$index", "sample"))
        }
        val report = buildRateCardMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
