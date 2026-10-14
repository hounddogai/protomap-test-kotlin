package com.acme.transport.freightpay.dispute.disputebundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DisputeRuleTest {
    @Test
    fun `starts in requested`() {
        val item = newDisputeRule("code-552", "title-766")
        assertEquals(DisputeRuleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newDisputeRule("code-552", "title-766")
        try {
            item.moveTo(DisputeRuleStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DisputeRuleStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDisputeRule("code-552", "title-766")
        item.status = DisputeRuleStatus.EXPIRED
        assertFails("move") { item.moveTo(DisputeRuleStatus.REQUESTED) }
    }
}
