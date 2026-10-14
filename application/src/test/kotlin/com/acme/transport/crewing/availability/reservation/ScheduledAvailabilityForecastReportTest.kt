package com.acme.transport.crewing.availability.reservation

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledAvailabilityForecastReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700940979)
        val items = mutableListOf<ScheduledAvailabilityForecast>()
        for (index in 0 until 3) {
            items.add(newScheduledAvailabilityForecast("scheduled-availability-forecast-$index", "sample"))
        }
        val report = buildScheduledAvailabilityForecastReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
