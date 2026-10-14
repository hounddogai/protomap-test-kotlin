package com.acme.transport.network.cutoff.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingCutoffDraftTest {
    @Test
    fun `starts in draft`() {
        val item = newRollingCutoffDraft("code-420", "name-964")
        assertEquals(RollingCutoffDraftStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newRollingCutoffDraft("code-420", "name-964")
        try {
            item.moveTo(RollingCutoffDraftStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingCutoffDraftStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRollingCutoffDraft("code-420", "name-964")
        item.status = RollingCutoffDraftStatus.CLOSED
        assertFails("move") { item.moveTo(RollingCutoffDraftStatus.DRAFT) }
    }
}
