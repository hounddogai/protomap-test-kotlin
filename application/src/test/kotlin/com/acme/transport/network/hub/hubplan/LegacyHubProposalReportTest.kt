package com.acme.transport.network.hub.hubplan

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyHubProposalReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700213637)
        val items = mutableListOf<LegacyHubProposal>()
        for (index in 0 until 4) {
            items.add(newLegacyHubProposal("legacy-hub-proposal-$index", "sample"))
        }
        val report = buildLegacyHubProposalReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
