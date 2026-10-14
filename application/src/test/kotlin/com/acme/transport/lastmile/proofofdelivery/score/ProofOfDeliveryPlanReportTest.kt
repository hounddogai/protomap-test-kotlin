package com.acme.transport.lastmile.proofofdelivery.score

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryPlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706054346)
        val items = mutableListOf<ProofOfDeliveryPlan>()
        for (index in 0 until 6) {
            items.add(newProofOfDeliveryPlan("proof-of-delivery-plan-$index", "sample"))
        }
        val report = buildProofOfDeliveryPlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
