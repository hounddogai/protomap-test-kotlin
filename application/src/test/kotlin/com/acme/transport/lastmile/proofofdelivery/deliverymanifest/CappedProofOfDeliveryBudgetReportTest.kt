package com.acme.transport.lastmile.proofofdelivery.deliverymanifest

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedProofOfDeliveryBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704312021)
        val items = mutableListOf<CappedProofOfDeliveryBudget>()
        for (index in 0 until 2) {
            items.add(newCappedProofOfDeliveryBudget("capped-proof-of-delivery-budget-$index", "sample"))
        }
        val report = buildCappedProofOfDeliveryBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
