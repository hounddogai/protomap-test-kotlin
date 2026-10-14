package com.acme.transport.linehaul.relay.relaythreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingRelayTierTest {
    @Test
    fun `starts in open`() {
        val item = newRollingRelayTier("code-96", "title-994")
        assertEquals(RollingRelayTierStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newRollingRelayTier("code-96", "title-994")
        try {
            item.moveTo(RollingRelayTierStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingRelayTierStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRollingRelayTier("code-96", "title-994")
        item.status = RollingRelayTierStatus.ARCHIVED
        assertFails("move") { item.moveTo(RollingRelayTierStatus.OPEN) }
    }
}
