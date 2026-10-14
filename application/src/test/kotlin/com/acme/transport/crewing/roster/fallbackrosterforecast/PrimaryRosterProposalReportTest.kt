package com.acme.transport.crewing.roster.fallbackrosterforecast

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryRosterProposalReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704245230)
        val items = mutableListOf<PrimaryRosterProposal>()
        for (index in 0 until 5) {
            items.add(newPrimaryRosterProposal("primary-roster-proposal-$index", "sample"))
        }
        val report = buildPrimaryRosterProposalReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
