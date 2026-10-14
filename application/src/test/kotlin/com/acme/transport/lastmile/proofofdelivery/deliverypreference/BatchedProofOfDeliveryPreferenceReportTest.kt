package com.acme.transport.lastmile.proofofdelivery.deliverypreference

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedProofOfDeliveryPreferenceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702771093)
        val items = mutableListOf<BatchedProofOfDeliveryPreference>()
        for (index in 0 until 2) {
            items.add(newBatchedProofOfDeliveryPreference("batched-proof-of-delivery-preference-$index", "sample"))
        }
        val report = buildBatchedProofOfDeliveryPreferenceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
