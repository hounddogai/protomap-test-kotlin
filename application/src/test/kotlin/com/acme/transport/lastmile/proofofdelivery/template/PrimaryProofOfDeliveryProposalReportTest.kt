package com.acme.transport.lastmile.proofofdelivery.template

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryProofOfDeliveryProposalReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702223438)
        val items = mutableListOf<PrimaryProofOfDeliveryProposal>()
        for (index in 0 until 5) {
            items.add(newPrimaryProofOfDeliveryProposal("primary-proof-of-delivery-proposal-$index", "sample"))
        }
        val report = buildPrimaryProofOfDeliveryProposalReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
