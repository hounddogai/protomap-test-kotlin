package com.acme.transport.network.transittime.score

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictTransitTimeScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709869528)
        val items = mutableListOf<StrictTransitTimeScore>()
        for (index in 0 until 2) {
            items.add(newStrictTransitTimeScore("strict-transit-time-score-$index", "sample"))
        }
        val report = buildStrictTransitTimeScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
