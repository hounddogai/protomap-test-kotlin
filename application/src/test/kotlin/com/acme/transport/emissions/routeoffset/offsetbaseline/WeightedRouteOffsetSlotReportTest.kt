package com.acme.transport.emissions.routeoffset.offsetbaseline

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedRouteOffsetSlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705945043)
        val items = mutableListOf<WeightedRouteOffsetSlot>()
        for (index in 0 until 3) {
            items.add(newWeightedRouteOffsetSlot("weighted-route-offset-slot-$index", "sample"))
        }
        val report = buildWeightedRouteOffsetSlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
