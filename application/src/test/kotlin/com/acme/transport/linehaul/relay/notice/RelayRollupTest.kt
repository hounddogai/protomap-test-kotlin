package com.acme.transport.linehaul.relay.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RelayRollupTest {
    @Test
    fun `starts in open`() {
        val item = newRelayRollup("code-877", "title-217")
        assertEquals(RelayRollupStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newRelayRollup("code-877", "title-217")
        try {
            item.moveTo(RelayRollupStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RelayRollupStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRelayRollup("code-877", "title-217")
        item.status = RelayRollupStatus.ARCHIVED
        assertFails("move") { item.moveTo(RelayRollupStatus.OPEN) }
    }
}
