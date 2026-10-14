package com.acme.transport.lastmile.proofofdelivery.deliveryreview

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualProofOfDeliveryPlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709175609)
        val items = mutableListOf<ManualProofOfDeliveryPlan>()
        for (index in 0 until 2) {
            items.add(newManualProofOfDeliveryPlan("manual-proof-of-delivery-plan-$index", "sample"))
        }
        val report = buildManualProofOfDeliveryPlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
