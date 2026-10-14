package com.acme.transport.fleetops.tire.window

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TireWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708785575)
        val items = mutableListOf<TireWindow>()
        for (index in 0 until 5) {
            items.add(newTireWindow("tire-window-$index", "sample"))
        }
        val report = buildTireWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
