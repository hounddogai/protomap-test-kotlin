package com.acme.transport.linehaul.relay.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RelayDigestTest {
    @Test
    fun `starts in proposed`() {
        val item = newRelayDigest("code-562", "name-61")
        assertEquals(RelayDigestStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newRelayDigest("code-562", "name-61")
        try {
            item.moveTo(RelayDigestStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RelayDigestStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRelayDigest("code-562", "name-61")
        item.status = RelayDigestStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RelayDigestStatus.PROPOSED) }
    }
}
