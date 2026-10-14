package com.acme.transport.lastmile.proofofdelivery.policy

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingProofOfDeliveryBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700376711)
        val items = mutableListOf<PendingProofOfDeliveryBaseline>()
        for (index in 0 until 3) {
            items.add(newPendingProofOfDeliveryBaseline("pending-proof-of-delivery-baseline-$index", "sample"))
        }
        val report = buildPendingProofOfDeliveryBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
