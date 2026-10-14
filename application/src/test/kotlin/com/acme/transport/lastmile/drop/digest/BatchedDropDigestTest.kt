package com.acme.transport.lastmile.drop.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedDropDigestTest {
    @Test
    fun `starts in idle`() {
        val item = newBatchedDropDigest("code-345", "title-812")
        assertEquals(BatchedDropDigestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newBatchedDropDigest("code-345", "title-812")
        try {
            item.moveTo(BatchedDropDigestStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedDropDigestStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newBatchedDropDigest("code-345", "title-812")
        item.status = BatchedDropDigestStatus.STOPPED
        assertFails("move") { item.moveTo(BatchedDropDigestStatus.IDLE) }
    }
}
