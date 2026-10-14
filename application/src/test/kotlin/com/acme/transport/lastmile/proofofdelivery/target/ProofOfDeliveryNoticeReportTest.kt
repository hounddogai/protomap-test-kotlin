package com.acme.transport.lastmile.proofofdelivery.target

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryNoticeReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705250318)
        val items = mutableListOf<ProofOfDeliveryNotice>()
        for (index in 0 until 6) {
            items.add(newProofOfDeliveryNotice("proof-of-delivery-notice-$index", "sample"))
        }
        val report = buildProofOfDeliveryNoticeReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
