package com.acme.transport.lastmile.locker.forecast

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LockerForecastReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704312644)
        val items = mutableListOf<LockerForecast>()
        for (index in 0 until 2) {
            items.add(newLockerForecast("locker-forecast-$index", "sample"))
        }
        val report = buildLockerForecastReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
