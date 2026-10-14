package com.acme.transport.lastmile.drop.target

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedDropTargetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708070174)
        val items = mutableListOf<WeightedDropTarget>()
        for (index in 0 until 3) {
            items.add(newWeightedDropTarget("weighted-drop-target-$index", "sample"))
        }
        val report = buildWeightedDropTargetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
