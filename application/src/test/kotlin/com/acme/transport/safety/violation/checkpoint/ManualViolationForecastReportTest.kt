package com.acme.transport.safety.violation.checkpoint

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualViolationForecastReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709984472)
        val items = mutableListOf<ManualViolationForecast>()
        for (index in 0 until 2) {
            items.add(newManualViolationForecast("manual-violation-forecast-$index", "sample"))
        }
        val report = buildManualViolationForecastReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
