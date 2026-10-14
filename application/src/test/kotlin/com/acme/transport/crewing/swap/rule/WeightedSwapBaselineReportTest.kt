package com.acme.transport.crewing.swap.rule

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedSwapBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708382322)
        val items = mutableListOf<WeightedSwapBaseline>()
        for (index in 0 until 5) {
            items.add(newWeightedSwapBaseline("weighted-swap-baseline-$index", "sample"))
        }
        val report = buildWeightedSwapBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
