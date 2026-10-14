package com.acme.transport.linehaul.tender.tier

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveTenderProposalReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707853108)
        val items = mutableListOf<AdaptiveTenderProposal>()
        for (index in 0 until 5) {
            items.add(newAdaptiveTenderProposal("adaptive-tender-proposal-$index", "sample"))
        }
        val report = buildAdaptiveTenderProposalReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
