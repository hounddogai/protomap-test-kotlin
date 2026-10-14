package com.acme.transport.lastmile.drop.batcheddropnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DropBatchTest {
    @Test
    fun `starts in open`() {
        val item = newDropBatch("code-143", "title-771")
        assertEquals(DropBatchStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newDropBatch("code-143", "title-771")
        try {
            item.moveTo(DropBatchStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DropBatchStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDropBatch("code-143", "title-771")
        item.status = DropBatchStatus.ARCHIVED
        assertFails("move") { item.moveTo(DropBatchStatus.OPEN) }
    }
}
