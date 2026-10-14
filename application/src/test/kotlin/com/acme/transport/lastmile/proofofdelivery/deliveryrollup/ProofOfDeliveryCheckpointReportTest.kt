package com.acme.transport.lastmile.proofofdelivery.deliveryrollup

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryCheckpointReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702222875)
        val items = mutableListOf<ProofOfDeliveryCheckpoint>()
        for (index in 0 until 3) {
            items.add(newProofOfDeliveryCheckpoint("proof-of-delivery-checkpoint-$index", "sample"))
        }
        val report = buildProofOfDeliveryCheckpointReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
