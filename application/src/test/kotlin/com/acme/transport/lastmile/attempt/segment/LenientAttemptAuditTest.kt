package com.acme.transport.lastmile.attempt.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientAttemptAuditTest {
    @Test
    fun `starts in open`() {
        val item = newLenientAttemptAudit("code-256", "name-257")
        assertEquals(LenientAttemptAuditStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newLenientAttemptAudit("code-256", "name-257")
        try {
            item.moveTo(LenientAttemptAuditStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientAttemptAuditStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLenientAttemptAudit("code-256", "name-257")
        item.status = LenientAttemptAuditStatus.ARCHIVED
        assertFails("move") { item.moveTo(LenientAttemptAuditStatus.OPEN) }
    }
}
