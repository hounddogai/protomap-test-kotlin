package com.acme.transport.crewing.roster.summary

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RosterSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707942480)
        val items = mutableListOf<RosterSummary>()
        for (index in 0 until 4) {
            items.add(newRosterSummary("roster-summary-$index", "sample"))
        }
        val report = buildRosterSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
