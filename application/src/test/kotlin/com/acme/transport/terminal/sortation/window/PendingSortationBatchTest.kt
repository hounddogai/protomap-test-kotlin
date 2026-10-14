package com.acme.transport.terminal.sortation.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingSortationBatchTest {
    @Test
    fun `starts in draft`() {
        val item = newPendingSortationBatch("code-337", "name-826")
        assertEquals(PendingSortationBatchStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newPendingSortationBatch("code-337", "name-826")
        try {
            item.moveTo(PendingSortationBatchStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingSortationBatchStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newPendingSortationBatch("code-337", "name-826")
        item.status = PendingSortationBatchStatus.CLOSED
        assertFails("move") { item.moveTo(PendingSortationBatchStatus.DRAFT) }
    }
}
