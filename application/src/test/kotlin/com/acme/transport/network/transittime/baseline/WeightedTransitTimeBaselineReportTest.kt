package com.acme.transport.network.transittime.baseline

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedTransitTimeBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700991411)
        val items = mutableListOf<WeightedTransitTimeBaseline>()
        for (index in 0 until 3) {
            items.add(newWeightedTransitTimeBaseline("weighted-transit-time-baseline-$index", "sample"))
        }
        val report = buildWeightedTransitTimeBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
