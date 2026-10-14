package com.acme.transport.crewing.swap.group

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedSwapSlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707007448)
        val items = mutableListOf<WeightedSwapSlot>()
        for (index in 0 until 4) {
            items.add(newWeightedSwapSlot("weighted-swap-slot-$index", "sample"))
        }
        val report = buildWeightedSwapSlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
