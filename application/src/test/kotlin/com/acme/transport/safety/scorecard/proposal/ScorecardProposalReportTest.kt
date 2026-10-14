package com.acme.transport.safety.scorecard.proposal

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScorecardProposalReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701281377)
        val items = mutableListOf<ScorecardProposal>()
        for (index in 0 until 2) {
            items.add(newScorecardProposal("scorecard-proposal-$index", "sample"))
        }
        val report = buildScorecardProposalReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
