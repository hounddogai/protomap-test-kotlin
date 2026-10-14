package com.acme.transport.lastmile.proofofdelivery.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientProofOfDeliveryRuleTest {
    @Test
    fun `starts in draft`() {
        val item = newLenientProofOfDeliveryRule("code-399", "label-324")
        assertEquals(LenientProofOfDeliveryRuleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newLenientProofOfDeliveryRule("code-399", "label-324")
        try {
            item.moveTo(LenientProofOfDeliveryRuleStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientProofOfDeliveryRuleStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLenientProofOfDeliveryRule("code-399", "label-324")
        item.status = LenientProofOfDeliveryRuleStatus.CLOSED
        assertFails("move") { item.moveTo(LenientProofOfDeliveryRuleStatus.DRAFT) }
    }
}
