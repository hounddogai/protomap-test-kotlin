package com.acme.transport.network.transittime.baseline

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredTransitTimeWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702554574)
        val items = mutableListOf<TieredTransitTimeWindow>()
        for (index in 0 until 5) {
            items.add(newTieredTransitTimeWindow("tiered-transit-time-window-$index", "sample"))
        }
        val report = buildTieredTransitTimeWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
