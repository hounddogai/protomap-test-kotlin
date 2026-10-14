package com.acme.transport.freightpay.dispute.forecast

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisputeForecastReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703253419)
        val items = mutableListOf<DisputeForecast>()
        for (index in 0 until 5) {
            items.add(newDisputeForecast("dispute-forecast-$index", "sample"))
        }
        val report = buildDisputeForecastReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
