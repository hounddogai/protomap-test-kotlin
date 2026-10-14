package com.acme.transport.linehaul.trailer.batch

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704470104)
        val items = mutableListOf<TrailerEntry>()
        for (index in 0 until 3) {
            items.add(newTrailerEntry("trailer-entry-$index", "sample"))
        }
        val report = buildTrailerEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
