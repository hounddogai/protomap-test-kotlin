package com.acme.transport.lastmile.proofofdelivery.deliverymanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedProofOfDeliveryBudgetTest {
    @Test
    fun `starts in created`() {
        val item = newCappedProofOfDeliveryBudget("code-542", "label-285")
        assertEquals(CappedProofOfDeliveryBudgetStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newCappedProofOfDeliveryBudget("code-542", "label-285")
        try {
            item.moveTo(CappedProofOfDeliveryBudgetStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedProofOfDeliveryBudgetStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCappedProofOfDeliveryBudget("code-542", "label-285")
        item.status = CappedProofOfDeliveryBudgetStatus.RETIRED
        assertFails("move") { item.moveTo(CappedProofOfDeliveryBudgetStatus.CREATED) }
    }
}
