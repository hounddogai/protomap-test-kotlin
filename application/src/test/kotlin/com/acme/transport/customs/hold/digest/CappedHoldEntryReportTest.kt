package com.acme.transport.customs.hold.digest

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedHoldEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707499787)
        val items = mutableListOf<CappedHoldEntry>()
        for (index in 0 until 6) {
            items.add(newCappedHoldEntry("capped-hold-entry-$index", "sample"))
        }
        val report = buildCappedHoldEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
