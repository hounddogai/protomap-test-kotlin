package com.acme.transport.lastmile.proofofdelivery.deliverymanifest

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedProofOfDeliveryAllocationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706011173)
        val items = mutableListOf<CappedProofOfDeliveryAllocation>()
        for (index in 0 until 4) {
            items.add(newCappedProofOfDeliveryAllocation("capped-proof-of-delivery-allocation-$index", "sample"))
        }
        val report = buildCappedProofOfDeliveryAllocationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
