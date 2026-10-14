package com.acme.transport.crewing.restbreak.baseline

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedRestBreakScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703530508)
        val items = mutableListOf<WeightedRestBreakScore>()
        for (index in 0 until 4) {
            items.add(newWeightedRestBreakScore("weighted-rest-break-score-$index", "sample"))
        }
        val report = buildWeightedRestBreakScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
