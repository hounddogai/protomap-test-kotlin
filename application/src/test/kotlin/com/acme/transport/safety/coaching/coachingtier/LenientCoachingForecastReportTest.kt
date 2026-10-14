package com.acme.transport.safety.coaching.coachingtier

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientCoachingForecastReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707640495)
        val items = mutableListOf<LenientCoachingForecast>()
        for (index in 0 until 6) {
            items.add(newLenientCoachingForecast("lenient-coaching-forecast-$index", "sample"))
        }
        val report = buildLenientCoachingForecastReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
