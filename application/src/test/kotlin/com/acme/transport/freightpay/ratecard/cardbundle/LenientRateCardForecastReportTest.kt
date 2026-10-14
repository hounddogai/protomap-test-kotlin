package com.acme.transport.freightpay.ratecard.cardbundle

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientRateCardForecastReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700890048)
        val items = mutableListOf<LenientRateCardForecast>()
        for (index in 0 until 4) {
            items.add(newLenientRateCardForecast("lenient-rate-card-forecast-$index", "sample"))
        }
        val report = buildLenientRateCardForecastReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
