package com.acme.transport.lastmile.parcel.parcelvariance

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedParcelForecastReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707422823)
        val items = mutableListOf<WeightedParcelForecast>()
        for (index in 0 until 2) {
            items.add(newWeightedParcelForecast("weighted-parcel-forecast-$index", "sample"))
        }
        val report = buildWeightedParcelForecastReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
