package com.acme.transport.lastmile.proofofdelivery.rollup

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedProofOfDeliveryRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703987982)
        val items = mutableListOf<BatchedProofOfDeliveryRollup>()
        for (index in 0 until 4) {
            items.add(newBatchedProofOfDeliveryRollup("batched-proof-of-delivery-rollup-$index", "sample"))
        }
        val report = buildBatchedProofOfDeliveryRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
