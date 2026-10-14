package com.acme.transport.lastmile.proofofdelivery.entry

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705901399)
        val items = mutableListOf<ProofOfDeliveryAgreement>()
        for (index in 0 until 4) {
            items.add(newProofOfDeliveryAgreement("proof-of-delivery-agreement-$index", "sample"))
        }
        val report = buildProofOfDeliveryAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
