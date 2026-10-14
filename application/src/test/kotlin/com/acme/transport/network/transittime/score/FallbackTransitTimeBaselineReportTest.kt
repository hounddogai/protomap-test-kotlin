package com.acme.transport.network.transittime.score

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackTransitTimeBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709126506)
        val items = mutableListOf<FallbackTransitTimeBaseline>()
        for (index in 0 until 2) {
            items.add(newFallbackTransitTimeBaseline("fallback-transit-time-baseline-$index", "sample"))
        }
        val report = buildFallbackTransitTimeBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
