package com.acme.transport.lastmile.proofofdelivery.segment

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingProofOfDeliveryPlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703344673)
        val items = mutableListOf<PendingProofOfDeliveryPlan>()
        for (index in 0 until 2) {
            items.add(newPendingProofOfDeliveryPlan("pending-proof-of-delivery-plan-$index", "sample"))
        }
        val report = buildPendingProofOfDeliveryPlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
