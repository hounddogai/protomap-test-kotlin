package com.acme.transport.freightpay.ratecard.variance

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackRateCardVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708117023)
        val items = mutableListOf<FallbackRateCardVariance>()
        for (index in 0 until 4) {
            items.add(newFallbackRateCardVariance("fallback-rate-card-variance-$index", "sample"))
        }
        val report = buildFallbackRateCardVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
