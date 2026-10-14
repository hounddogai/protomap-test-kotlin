package com.acme.transport.terminal.dwell.bundle

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveDwellEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704428312)
        val items = mutableListOf<AdaptiveDwellEntry>()
        for (index in 0 until 4) {
            items.add(newAdaptiveDwellEntry("adaptive-dwell-entry-$index", "sample"))
        }
        val report = buildAdaptiveDwellEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
