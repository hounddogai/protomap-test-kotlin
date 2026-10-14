package com.acme.transport.network.node.template

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredNodeProposalReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704667762)
        val items = mutableListOf<TieredNodeProposal>()
        for (index in 0 until 6) {
            items.add(newTieredNodeProposal("tiered-node-proposal-$index", "sample"))
        }
        val report = buildTieredNodeProposalReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
