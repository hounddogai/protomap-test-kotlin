package com.acme.transport.linehaul.relay.relaythreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyRelayRuleTest {
    @Test
    fun `starts in idle`() {
        val item = newLegacyRelayRule("code-537", "title-711")
        assertEquals(LegacyRelayRuleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newLegacyRelayRule("code-537", "title-711")
        try {
            item.moveTo(LegacyRelayRuleStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyRelayRuleStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLegacyRelayRule("code-537", "title-711")
        item.status = LegacyRelayRuleStatus.STOPPED
        assertFails("move") { item.moveTo(LegacyRelayRuleStatus.IDLE) }
    }
}
