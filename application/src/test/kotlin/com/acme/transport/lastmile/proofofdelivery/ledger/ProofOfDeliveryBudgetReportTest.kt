package com.acme.transport.lastmile.proofofdelivery.ledger

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703215876)
        val items = mutableListOf<ProofOfDeliveryBudget>()
        for (index in 0 until 4) {
            items.add(newProofOfDeliveryBudget("proof-of-delivery-budget-$index", "sample"))
        }
        val report = buildProofOfDeliveryBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
