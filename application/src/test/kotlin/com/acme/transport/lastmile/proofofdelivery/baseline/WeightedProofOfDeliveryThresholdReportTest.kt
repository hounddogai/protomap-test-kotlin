package com.acme.transport.lastmile.proofofdelivery.baseline

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedProofOfDeliveryThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708408861)
        val items = mutableListOf<WeightedProofOfDeliveryThreshold>()
        for (index in 0 until 4) {
            items.add(newWeightedProofOfDeliveryThreshold("weighted-proof-of-delivery-threshold-$index", "sample"))
        }
        val report = buildWeightedProofOfDeliveryThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
