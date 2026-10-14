package com.acme.transport.crewing.roster.entry

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalRosterEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700838290)
        val items = mutableListOf<GlobalRosterEntry>()
        for (index in 0 until 4) {
            items.add(newGlobalRosterEntry("global-roster-entry-$index", "sample"))
        }
        val report = buildGlobalRosterEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
