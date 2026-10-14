package com.acme.transport.crewing.roster.rosterforecast

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingRosterEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707709557)
        val items = mutableListOf<RollingRosterEntry>()
        for (index in 0 until 4) {
            items.add(newRollingRosterEntry("rolling-roster-entry-$index", "sample"))
        }
        val report = buildRollingRosterEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
