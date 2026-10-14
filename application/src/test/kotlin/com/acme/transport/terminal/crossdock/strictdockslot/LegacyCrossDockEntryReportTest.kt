package com.acme.transport.terminal.crossdock.strictdockslot

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyCrossDockEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708116103)
        val items = mutableListOf<LegacyCrossDockEntry>()
        for (index in 0 until 2) {
            items.add(newLegacyCrossDockEntry("legacy-cross-dock-entry-$index", "sample"))
        }
        val report = buildLegacyCrossDockEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
