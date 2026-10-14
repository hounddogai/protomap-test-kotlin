package com.acme.transport.safety.scorecard.rule

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingScorecardForecastReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703385681)
        val items = mutableListOf<PendingScorecardForecast>()
        for (index in 0 until 4) {
            items.add(newPendingScorecardForecast("pending-scorecard-forecast-$index", "sample"))
        }
        val report = buildPendingScorecardForecastReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
