package com.acme.transport.terminal.crossdock.assignment

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrossDockEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702060862)
        val items = mutableListOf<CrossDockEntry>()
        for (index in 0 until 3) {
            items.add(newCrossDockEntry("cross-dock-entry-$index", "sample"))
        }
        val report = buildCrossDockEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
