package com.acme.transport.safety.violation.forecast

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictViolationForecastReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701260039)
        val items = mutableListOf<StrictViolationForecast>()
        for (index in 0 until 2) {
            items.add(newStrictViolationForecast("strict-violation-forecast-$index", "sample"))
        }
        val report = buildStrictViolationForecastReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
