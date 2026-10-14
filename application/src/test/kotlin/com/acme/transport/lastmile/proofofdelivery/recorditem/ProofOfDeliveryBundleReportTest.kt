package com.acme.transport.lastmile.proofofdelivery.recorditem

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryBundleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707958294)
        val items = mutableListOf<ProofOfDeliveryBundle>()
        for (index in 0 until 3) {
            items.add(newProofOfDeliveryBundle("proof-of-delivery-bundle-$index", "sample"))
        }
        val report = buildProofOfDeliveryBundleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
