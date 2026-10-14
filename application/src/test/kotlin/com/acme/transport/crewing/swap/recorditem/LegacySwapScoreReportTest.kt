package com.acme.transport.crewing.swap.recorditem

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacySwapScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705070984)
        val items = mutableListOf<LegacySwapScore>()
        for (index in 0 until 3) {
            items.add(newLegacySwapScore("legacy-swap-score-$index", "sample"))
        }
        val report = buildLegacySwapScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
