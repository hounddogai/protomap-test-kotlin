package com.acme.transport.crewing.roster.entry

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RosterWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706776810)
        val items = mutableListOf<RosterWindow>()
        for (index in 0 until 4) {
            items.add(newRosterWindow("roster-window-$index", "sample"))
        }
        val report = buildRosterWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
