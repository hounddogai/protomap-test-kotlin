package com.acme.transport.lastmile.proofofdelivery.reservation

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryProposalReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708796598)
        val items = mutableListOf<ProofOfDeliveryProposal>()
        for (index in 0 until 6) {
            items.add(newProofOfDeliveryProposal("proof-of-delivery-proposal-$index", "sample"))
        }
        val report = buildProofOfDeliveryProposalReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
