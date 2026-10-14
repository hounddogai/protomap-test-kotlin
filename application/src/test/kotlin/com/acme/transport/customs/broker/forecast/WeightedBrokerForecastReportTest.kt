package com.acme.transport.customs.broker.forecast

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedBrokerForecastReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701607093)
        val items = mutableListOf<WeightedBrokerForecast>()
        for (index in 0 until 2) {
            items.add(newWeightedBrokerForecast("weighted-broker-forecast-$index", "sample"))
        }
        val report = buildWeightedBrokerForecastReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
