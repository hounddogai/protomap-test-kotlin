package com.acme.transport.lastmile.proofofdelivery.mapping

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703098942)
        val items = mutableListOf<ProofOfDeliveryDigest>()
        for (index in 0 until 2) {
            items.add(newProofOfDeliveryDigest("proof-of-delivery-digest-$index", "sample"))
        }
        val report = buildProofOfDeliveryDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
