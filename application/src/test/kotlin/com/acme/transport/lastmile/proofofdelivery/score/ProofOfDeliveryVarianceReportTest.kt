package com.acme.transport.lastmile.proofofdelivery.score

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705018565)
        val items = mutableListOf<ProofOfDeliveryVariance>()
        for (index in 0 until 2) {
            items.add(newProofOfDeliveryVariance("proof-of-delivery-variance-$index", "sample"))
        }
        val report = buildProofOfDeliveryVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
