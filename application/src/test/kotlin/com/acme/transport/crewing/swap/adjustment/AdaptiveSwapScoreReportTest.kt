package com.acme.transport.crewing.swap.adjustment

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveSwapScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708271126)
        val items = mutableListOf<AdaptiveSwapScore>()
        for (index in 0 until 2) {
            items.add(newAdaptiveSwapScore("adaptive-swap-score-$index", "sample"))
        }
        val report = buildAdaptiveSwapScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
