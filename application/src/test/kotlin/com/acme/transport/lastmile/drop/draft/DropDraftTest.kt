package com.acme.transport.lastmile.drop.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DropDraftTest {
    @Test
    fun `starts in idle`() {
        val item = newDropDraft("code-107", "label-407")
        assertEquals(DropDraftStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newDropDraft("code-107", "label-407")
        try {
            item.moveTo(DropDraftStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DropDraftStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDropDraft("code-107", "label-407")
        item.status = DropDraftStatus.STOPPED
        assertFails("move") { item.moveTo(DropDraftStatus.IDLE) }
    }
}
