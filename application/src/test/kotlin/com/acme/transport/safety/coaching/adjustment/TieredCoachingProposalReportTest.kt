package com.acme.transport.safety.coaching.adjustment

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredCoachingProposalReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703516627)
        val items = mutableListOf<TieredCoachingProposal>()
        for (index in 0 until 3) {
            items.add(newTieredCoachingProposal("tiered-coaching-proposal-$index", "sample"))
        }
        val report = buildTieredCoachingProposalReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
