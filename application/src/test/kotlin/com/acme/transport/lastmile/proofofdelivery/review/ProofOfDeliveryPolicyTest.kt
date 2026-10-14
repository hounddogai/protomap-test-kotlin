package com.acme.transport.lastmile.proofofdelivery.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ProofOfDeliveryPolicyTest {
    @Test
    fun `starts in requested`() {
        val item = newProofOfDeliveryPolicy("code-142", "name-173")
        assertEquals(ProofOfDeliveryPolicyStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newProofOfDeliveryPolicy("code-142", "name-173")
        try {
            item.moveTo(ProofOfDeliveryPolicyStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ProofOfDeliveryPolicyStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newProofOfDeliveryPolicy("code-142", "name-173")
        item.status = ProofOfDeliveryPolicyStatus.EXPIRED
        assertFails("move") { item.moveTo(ProofOfDeliveryPolicyStatus.REQUESTED) }
    }
}
