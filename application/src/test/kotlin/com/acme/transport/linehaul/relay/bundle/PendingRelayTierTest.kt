package com.acme.transport.linehaul.relay.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingRelayTierTest {
    @Test
    fun `starts in open`() {
        val item = newPendingRelayTier("code-973", "title-27")
        assertEquals(PendingRelayTierStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newPendingRelayTier("code-973", "title-27")
        try {
            item.moveTo(PendingRelayTierStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingRelayTierStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPendingRelayTier("code-973", "title-27")
        item.status = PendingRelayTierStatus.ARCHIVED
        assertFails("move") { item.moveTo(PendingRelayTierStatus.OPEN) }
    }
}
