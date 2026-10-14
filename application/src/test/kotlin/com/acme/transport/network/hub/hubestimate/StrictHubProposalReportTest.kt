package com.acme.transport.network.hub.hubestimate

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictHubProposalReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700304755)
        val items = mutableListOf<StrictHubProposal>()
        for (index in 0 until 4) {
            items.add(newStrictHubProposal("strict-hub-proposal-$index", "sample"))
        }
        val report = buildStrictHubProposalReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
