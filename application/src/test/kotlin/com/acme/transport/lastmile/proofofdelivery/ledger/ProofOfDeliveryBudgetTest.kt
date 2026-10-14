package com.acme.transport.lastmile.proofofdelivery.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ProofOfDeliveryBudgetTest {
    @Test
    fun `starts in draft`() {
        val item = newProofOfDeliveryBudget("code-999", "name-987")
        assertEquals(ProofOfDeliveryBudgetStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newProofOfDeliveryBudget("code-999", "name-987")
        try {
            item.moveTo(ProofOfDeliveryBudgetStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ProofOfDeliveryBudgetStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newProofOfDeliveryBudget("code-999", "name-987")
        item.status = ProofOfDeliveryBudgetStatus.CLOSED
        assertFails("move") { item.moveTo(ProofOfDeliveryBudgetStatus.DRAFT) }
    }
}
