package com.acme.transport.terminal.sortation.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimarySortationBatchTest {
    @Test
    fun `starts in draft`() {
        val item = newPrimarySortationBatch("code-132", "title-9")
        assertEquals(PrimarySortationBatchStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newPrimarySortationBatch("code-132", "title-9")
        try {
            item.moveTo(PrimarySortationBatchStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimarySortationBatchStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newPrimarySortationBatch("code-132", "title-9")
        item.status = PrimarySortationBatchStatus.CLOSED
        assertFails("move") { item.moveTo(PrimarySortationBatchStatus.DRAFT) }
    }
}
