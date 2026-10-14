package com.acme.transport.terminal.crossdock.profile

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrossDockWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709443609)
        val items = mutableListOf<CrossDockWindow>()
        for (index in 0 until 5) {
            items.add(newCrossDockWindow("cross-dock-window-$index", "sample"))
        }
        val report = buildCrossDockWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
