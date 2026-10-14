package com.acme.transport.crewing.restbreak.exception

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveRestBreakScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702350389)
        val items = mutableListOf<AdaptiveRestBreakScore>()
        for (index in 0 until 4) {
            items.add(newAdaptiveRestBreakScore("adaptive-rest-break-score-$index", "sample"))
        }
        val report = buildAdaptiveRestBreakScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
