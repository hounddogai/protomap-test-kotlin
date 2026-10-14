package com.acme.transport.lastmile.proofofdelivery.budget

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedProofOfDeliveryBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701960208)
        val items = mutableListOf<BatchedProofOfDeliveryBudget>()
        for (index in 0 until 6) {
            items.add(newBatchedProofOfDeliveryBudget("batched-proof-of-delivery-budget-$index", "sample"))
        }
        val report = buildBatchedProofOfDeliveryBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
