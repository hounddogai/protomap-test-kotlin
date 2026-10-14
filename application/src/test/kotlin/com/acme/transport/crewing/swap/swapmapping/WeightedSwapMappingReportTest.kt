package com.acme.transport.crewing.swap.swapmapping

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedSwapMappingReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702587492)
        val items = mutableListOf<WeightedSwapMapping>()
        for (index in 0 until 2) {
            items.add(newWeightedSwapMapping("weighted-swap-mapping-$index", "sample"))
        }
        val report = buildWeightedSwapMappingReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
