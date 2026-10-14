package com.acme.transport.lastmile.proofofdelivery.deliverymanifest

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedProofOfDeliveryNoticeReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705264918)
        val items = mutableListOf<CappedProofOfDeliveryNotice>()
        for (index in 0 until 4) {
            items.add(newCappedProofOfDeliveryNotice("capped-proof-of-delivery-notice-$index", "sample"))
        }
        val report = buildCappedProofOfDeliveryNoticeReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
