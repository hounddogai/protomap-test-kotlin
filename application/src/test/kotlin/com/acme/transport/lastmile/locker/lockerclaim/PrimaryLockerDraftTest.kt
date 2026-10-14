package com.acme.transport.lastmile.locker.lockerclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryLockerDraftTest {
    @Test
    fun `starts in new`() {
        val item = newPrimaryLockerDraft("code-913", "title-851")
        assertEquals(PrimaryLockerDraftStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newPrimaryLockerDraft("code-913", "title-851")
        try {
            item.moveTo(PrimaryLockerDraftStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryLockerDraftStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPrimaryLockerDraft("code-913", "title-851")
        item.status = PrimaryLockerDraftStatus.FAILED
        assertFails("move") { item.moveTo(PrimaryLockerDraftStatus.NEW) }
    }
}
