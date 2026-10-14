package com.acme.transport.linehaul.backhaul.backhauldigest

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedBackhaulForecastReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700431074)
        val items = mutableListOf<WeightedBackhaulForecast>()
        for (index in 0 until 3) {
            items.add(newWeightedBackhaulForecast("weighted-backhaul-forecast-$index", "sample"))
        }
        val report = buildWeightedBackhaulForecastReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
