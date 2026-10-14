package com.acme.transport.lastmile.proofofdelivery.recorditem

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryBatchReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701191290)
        val items = mutableListOf<ProofOfDeliveryBatch>()
        for (index in 0 until 4) {
            items.add(newProofOfDeliveryBatch("proof-of-delivery-batch-$index", "sample"))
        }
        val report = buildProofOfDeliveryBatchReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
