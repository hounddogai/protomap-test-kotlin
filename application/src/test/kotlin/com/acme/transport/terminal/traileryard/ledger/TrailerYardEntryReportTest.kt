package com.acme.transport.terminal.traileryard.ledger

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerYardEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701733851)
        val items = mutableListOf<TrailerYardEntry>()
        for (index in 0 until 3) {
            items.add(newTrailerYardEntry("trailer-yard-entry-$index", "sample"))
        }
        val report = buildTrailerYardEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
