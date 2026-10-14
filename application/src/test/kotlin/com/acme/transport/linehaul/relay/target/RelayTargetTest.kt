package com.acme.transport.linehaul.relay.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RelayTargetTest {
    @Test
    fun `starts in proposed`() {
        val item = newRelayTarget("code-134", "title-66")
        assertEquals(RelayTargetStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newRelayTarget("code-134", "title-66")
        try {
            item.moveTo(RelayTargetStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RelayTargetStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRelayTarget("code-134", "title-66")
        item.status = RelayTargetStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RelayTargetStatus.PROPOSED) }
    }
}
