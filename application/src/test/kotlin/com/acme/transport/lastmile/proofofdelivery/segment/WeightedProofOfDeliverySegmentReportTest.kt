package com.acme.transport.lastmile.proofofdelivery.segment

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedProofOfDeliverySegmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700483408)
        val items = mutableListOf<WeightedProofOfDeliverySegment>()
        for (index in 0 until 5) {
            items.add(newWeightedProofOfDeliverySegment("weighted-proof-of-delivery-segment-$index", "sample"))
        }
        val report = buildWeightedProofOfDeliverySegmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
