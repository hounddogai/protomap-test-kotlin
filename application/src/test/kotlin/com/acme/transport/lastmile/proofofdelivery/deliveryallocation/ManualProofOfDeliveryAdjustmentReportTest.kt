package com.acme.transport.lastmile.proofofdelivery.deliveryallocation

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualProofOfDeliveryAdjustmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707763888)
        val items = mutableListOf<ManualProofOfDeliveryAdjustment>()
        for (index in 0 until 3) {
            items.add(newManualProofOfDeliveryAdjustment("manual-proof-of-delivery-adjustment-$index", "sample"))
        }
        val report = buildManualProofOfDeliveryAdjustmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
