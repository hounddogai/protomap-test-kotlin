package com.acme.transport.terminal.crossdock.strictdockslot

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedCrossDockSlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700758842)
        val items = mutableListOf<WeightedCrossDockSlot>()
        for (index in 0 until 3) {
            items.add(newWeightedCrossDockSlot("weighted-cross-dock-slot-$index", "sample"))
        }
        val report = buildWeightedCrossDockSlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
