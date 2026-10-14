package com.acme.transport.terminal.crossdock.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualCrossDockAuditTest {
    @Test
    fun `starts in draft`() {
        val item = newManualCrossDockAudit("code-826", "name-448")
        assertEquals(ManualCrossDockAuditStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newManualCrossDockAudit("code-826", "name-448")
        try {
            item.moveTo(ManualCrossDockAuditStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualCrossDockAuditStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newManualCrossDockAudit("code-826", "name-448")
        item.status = ManualCrossDockAuditStatus.CLOSED
        assertFails("move") { item.moveTo(ManualCrossDockAuditStatus.DRAFT) }
    }
}
