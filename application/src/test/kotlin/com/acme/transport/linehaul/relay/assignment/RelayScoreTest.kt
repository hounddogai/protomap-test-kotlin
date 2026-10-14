package com.acme.transport.linehaul.relay.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RelayScoreTest {
    @Test
    fun `starts in open`() {
        val item = newRelayScore("code-434", "label-695")
        assertEquals(RelayScoreStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newRelayScore("code-434", "label-695")
        try {
            item.moveTo(RelayScoreStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RelayScoreStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRelayScore("code-434", "label-695")
        item.status = RelayScoreStatus.ARCHIVED
        assertFails("move") { item.moveTo(RelayScoreStatus.OPEN) }
    }
}
