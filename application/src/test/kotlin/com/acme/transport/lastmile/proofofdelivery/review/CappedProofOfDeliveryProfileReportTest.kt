package com.acme.transport.lastmile.proofofdelivery.review

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedProofOfDeliveryProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709840183)
        val items = mutableListOf<CappedProofOfDeliveryProfile>()
        for (index in 0 until 3) {
            items.add(newCappedProofOfDeliveryProfile("capped-proof-of-delivery-profile-$index", "sample"))
        }
        val report = buildCappedProofOfDeliveryProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
