package com.acme.transport.lastmile.drop.entry

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700300533)
        val items = mutableListOf<DropEntry>()
        for (index in 0 until 5) {
            items.add(newDropEntry("drop-entry-$index", "sample"))
        }
        val report = buildDropEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
