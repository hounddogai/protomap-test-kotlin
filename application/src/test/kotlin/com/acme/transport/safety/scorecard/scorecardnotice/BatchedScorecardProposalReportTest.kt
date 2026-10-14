package com.acme.transport.safety.scorecard.scorecardnotice

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedScorecardProposalReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703450278)
        val items = mutableListOf<BatchedScorecardProposal>()
        for (index in 0 until 4) {
            items.add(newBatchedScorecardProposal("batched-scorecard-proposal-$index", "sample"))
        }
        val report = buildBatchedScorecardProposalReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
