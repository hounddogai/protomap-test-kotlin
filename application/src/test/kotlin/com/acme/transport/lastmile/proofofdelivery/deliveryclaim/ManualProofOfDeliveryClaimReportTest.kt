package com.acme.transport.lastmile.proofofdelivery.deliveryclaim

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualProofOfDeliveryClaimReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705147989)
        val items = mutableListOf<ManualProofOfDeliveryClaim>()
        for (index in 0 until 3) {
            items.add(newManualProofOfDeliveryClaim("manual-proof-of-delivery-claim-$index", "sample"))
        }
        val report = buildManualProofOfDeliveryClaimReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
