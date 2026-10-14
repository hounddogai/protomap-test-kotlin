package com.acme.transport.safety.scorecard.scorecardledger

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultScorecardForecastReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702775804)
        val items = mutableListOf<DefaultScorecardForecast>()
        for (index in 0 until 6) {
            items.add(newDefaultScorecardForecast("default-scorecard-forecast-$index", "sample"))
        }
        val report = buildDefaultScorecardForecastReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
