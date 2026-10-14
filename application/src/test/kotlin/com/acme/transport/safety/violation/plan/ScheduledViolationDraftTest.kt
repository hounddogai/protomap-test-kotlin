package com.acme.transport.safety.violation.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledViolationDraftTest {
    @Test
    fun `starts in open`() {
        val item = newScheduledViolationDraft("code-572", "name-490")
        assertEquals(ScheduledViolationDraftStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newScheduledViolationDraft("code-572", "name-490")
        try {
            item.moveTo(ScheduledViolationDraftStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledViolationDraftStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newScheduledViolationDraft("code-572", "name-490")
        item.status = ScheduledViolationDraftStatus.ARCHIVED
        assertFails("move") { item.moveTo(ScheduledViolationDraftStatus.OPEN) }
    }
}
