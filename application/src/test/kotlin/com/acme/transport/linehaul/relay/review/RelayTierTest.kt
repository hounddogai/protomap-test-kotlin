package com.acme.transport.linehaul.relay.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RelayTierTest {
    @Test
    fun `starts in requested`() {
        val item = newRelayTier("code-530", "name-487")
        assertEquals(RelayTierStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newRelayTier("code-530", "name-487")
        try {
            item.moveTo(RelayTierStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RelayTierStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRelayTier("code-530", "name-487")
        item.status = RelayTierStatus.EXPIRED
        assertFails("move") { item.moveTo(RelayTierStatus.REQUESTED) }
    }
}
