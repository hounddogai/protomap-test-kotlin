package com.acme.transport.lastmile.proofofdelivery.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ProofOfDeliveryRuleTest {
    @Test
    fun `starts in requested`() {
        val item = newProofOfDeliveryRule("code-957", "label-960")
        assertEquals(ProofOfDeliveryRuleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newProofOfDeliveryRule("code-957", "label-960")
        try {
            item.moveTo(ProofOfDeliveryRuleStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ProofOfDeliveryRuleStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newProofOfDeliveryRule("code-957", "label-960")
        item.status = ProofOfDeliveryRuleStatus.EXPIRED
        assertFails("move") { item.moveTo(ProofOfDeliveryRuleStatus.REQUESTED) }
    }
}
