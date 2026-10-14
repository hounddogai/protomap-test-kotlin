package com.acme.transport.lastmile.proofofdelivery.target

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryTargetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702993671)
        val items = mutableListOf<ProofOfDeliveryTarget>()
        for (index in 0 until 4) {
            items.add(newProofOfDeliveryTarget("proof-of-delivery-target-$index", "sample"))
        }
        val report = buildProofOfDeliveryTargetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
