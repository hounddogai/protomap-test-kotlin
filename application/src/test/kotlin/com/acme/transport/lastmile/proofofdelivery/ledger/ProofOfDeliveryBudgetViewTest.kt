package com.acme.transport.lastmile.proofofdelivery.ledger

import kotlin.test.Test
import kotlin.test.assertEquals

class ProofOfDeliveryBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newProofOfDeliveryBudget("code-999", "name-987")
        val view = toProofOfDeliveryBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toProofOfDeliveryBudgetViews(mutableListOf<ProofOfDeliveryBudget>(item, item)).size, "views")
    }
}
