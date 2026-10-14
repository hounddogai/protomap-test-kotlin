package com.acme.transport.linehaul.relay.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RelayAuditTest {
    @Test
    fun `starts in open`() {
        val item = newRelayAudit("code-855", "name-553")
        assertEquals(RelayAuditStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newRelayAudit("code-855", "name-553")
        try {
            item.moveTo(RelayAuditStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RelayAuditStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRelayAudit("code-855", "name-553")
        item.status = RelayAuditStatus.ARCHIVED
        assertFails("move") { item.moveTo(RelayAuditStatus.OPEN) }
    }
}
