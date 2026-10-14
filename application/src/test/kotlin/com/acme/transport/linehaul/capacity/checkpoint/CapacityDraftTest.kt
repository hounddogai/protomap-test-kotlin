package com.acme.transport.linehaul.capacity.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CapacityDraftTest {
    @Test
    fun `starts in new`() {
        val item = newCapacityDraft("code-200", "title-753")
        assertEquals(CapacityDraftStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newCapacityDraft("code-200", "title-753")
        try {
            item.moveTo(CapacityDraftStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CapacityDraftStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCapacityDraft("code-200", "title-753")
        item.status = CapacityDraftStatus.FAILED
        assertFails("move") { item.moveTo(CapacityDraftStatus.NEW) }
    }
}
