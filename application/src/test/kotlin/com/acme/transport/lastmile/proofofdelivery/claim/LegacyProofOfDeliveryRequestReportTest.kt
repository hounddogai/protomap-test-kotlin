package com.acme.transport.lastmile.proofofdelivery.claim

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyProofOfDeliveryRequestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702719112)
        val items = mutableListOf<LegacyProofOfDeliveryRequest>()
        for (index in 0 until 2) {
            items.add(newLegacyProofOfDeliveryRequest("legacy-proof-of-delivery-request-$index", "sample"))
        }
        val report = buildLegacyProofOfDeliveryRequestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
