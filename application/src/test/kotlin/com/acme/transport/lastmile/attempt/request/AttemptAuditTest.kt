package com.acme.transport.lastmile.attempt.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AttemptAuditTest {
    @Test
    fun `starts in open`() {
        val item = newAttemptAudit("code-884", "title-194")
        assertEquals(AttemptAuditStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newAttemptAudit("code-884", "title-194")
        try {
            item.moveTo(AttemptAuditStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AttemptAuditStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAttemptAudit("code-884", "title-194")
        item.status = AttemptAuditStatus.ARCHIVED
        assertFails("move") { item.moveTo(AttemptAuditStatus.OPEN) }
    }
}
