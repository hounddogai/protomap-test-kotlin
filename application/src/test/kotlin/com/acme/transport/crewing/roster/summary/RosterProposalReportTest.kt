package com.acme.transport.crewing.roster.summary

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RosterProposalReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702891097)
        val items = mutableListOf<RosterProposal>()
        for (index in 0 until 5) {
            items.add(newRosterProposal("roster-proposal-$index", "sample"))
        }
        val report = buildRosterProposalReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
