package com.acme.transport.linehaul.relay.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RelayOverrideTest {
    @Test
    fun `starts in open`() {
        val item = newRelayOverride("code-527", "name-5")
        assertEquals(RelayOverrideStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newRelayOverride("code-527", "name-5")
        try {
            item.moveTo(RelayOverrideStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RelayOverrideStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRelayOverride("code-527", "name-5")
        item.status = RelayOverrideStatus.ARCHIVED
        assertFails("move") { item.moveTo(RelayOverrideStatus.OPEN) }
    }
}
