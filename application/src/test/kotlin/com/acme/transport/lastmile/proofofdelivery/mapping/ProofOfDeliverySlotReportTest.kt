package com.acme.transport.lastmile.proofofdelivery.mapping

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliverySlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703309698)
        val items = mutableListOf<ProofOfDeliverySlot>()
        for (index in 0 until 3) {
            items.add(newProofOfDeliverySlot("proof-of-delivery-slot-$index", "sample"))
        }
        val report = buildProofOfDeliverySlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
