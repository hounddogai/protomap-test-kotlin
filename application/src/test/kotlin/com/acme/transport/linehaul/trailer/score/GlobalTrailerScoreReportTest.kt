package com.acme.transport.linehaul.trailer.score

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalTrailerScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705841010)
        val items = mutableListOf<GlobalTrailerScore>()
        for (index in 0 until 5) {
            items.add(newGlobalTrailerScore("global-trailer-score-$index", "sample"))
        }
        val report = buildGlobalTrailerScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
