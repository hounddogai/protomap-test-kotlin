package com.acme.transport.terminal.dwell.schedule

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedDwellEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705023655)
        val items = mutableListOf<CappedDwellEntry>()
        for (index in 0 until 2) {
            items.add(newCappedDwellEntry("capped-dwell-entry-$index", "sample"))
        }
        val report = buildCappedDwellEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
