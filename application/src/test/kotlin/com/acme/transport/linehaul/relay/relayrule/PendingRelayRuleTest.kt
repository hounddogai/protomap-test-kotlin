package com.acme.transport.linehaul.relay.relayrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingRelayRuleTest {
    @Test
    fun `starts in proposed`() {
        val item = newPendingRelayRule("code-977", "title-382")
        assertEquals(PendingRelayRuleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newPendingRelayRule("code-977", "title-382")
        try {
            item.moveTo(PendingRelayRuleStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingRelayRuleStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPendingRelayRule("code-977", "title-382")
        item.status = PendingRelayRuleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PendingRelayRuleStatus.PROPOSED) }
    }
}
