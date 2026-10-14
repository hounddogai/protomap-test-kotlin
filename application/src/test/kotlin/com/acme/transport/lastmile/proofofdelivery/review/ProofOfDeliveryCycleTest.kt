package com.acme.transport.lastmile.proofofdelivery.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ProofOfDeliveryCycleTest {
    @Test
    fun `starts in proposed`() {
        val item = newProofOfDeliveryCycle("code-451", "title-870")
        assertEquals(ProofOfDeliveryCycleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newProofOfDeliveryCycle("code-451", "title-870")
        try {
            item.moveTo(ProofOfDeliveryCycleStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ProofOfDeliveryCycleStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newProofOfDeliveryCycle("code-451", "title-870")
        item.status = ProofOfDeliveryCycleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ProofOfDeliveryCycleStatus.PROPOSED) }
    }
}
