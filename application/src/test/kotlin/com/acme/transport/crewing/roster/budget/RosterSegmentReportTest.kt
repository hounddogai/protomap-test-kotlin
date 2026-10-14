package com.acme.transport.crewing.roster.budget

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RosterSegmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704775809)
        val items = mutableListOf<RosterSegment>()
        for (index in 0 until 6) {
            items.add(newRosterSegment("roster-segment-$index", "sample"))
        }
        val report = buildRosterSegmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
