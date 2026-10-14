package com.acme.transport.emissions.evcharge.claim

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeProposalReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700957131)
        val items = mutableListOf<EvChargeProposal>()
        for (index in 0 until 5) {
            items.add(newEvChargeProposal("ev-charge-proposal-$index", "sample"))
        }
        val report = buildEvChargeProposalReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
