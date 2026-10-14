package com.acme.transport.lastmile.proofofdelivery.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ProofOfDeliveryPlanTest {
    @Test
    fun `starts in requested`() {
        val item = newProofOfDeliveryPlan("code-956", "title-337")
        assertEquals(ProofOfDeliveryPlanStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newProofOfDeliveryPlan("code-956", "title-337")
        try {
            item.moveTo(ProofOfDeliveryPlanStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ProofOfDeliveryPlanStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newProofOfDeliveryPlan("code-956", "title-337")
        item.status = ProofOfDeliveryPlanStatus.EXPIRED
        assertFails("move") { item.moveTo(ProofOfDeliveryPlanStatus.REQUESTED) }
    }
}
