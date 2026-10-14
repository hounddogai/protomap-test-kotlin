package com.acme.transport.terminal.sortation.sortationvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedSortationOverrideTest {
    @Test
    fun `starts in draft`() {
        val item = newBatchedSortationOverride("code-901", "label-12")
        assertEquals(BatchedSortationOverrideStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newBatchedSortationOverride("code-901", "label-12")
        try {
            item.moveTo(BatchedSortationOverrideStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedSortationOverrideStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newBatchedSortationOverride("code-901", "label-12")
        item.status = BatchedSortationOverrideStatus.CLOSED
        assertFails("move") { item.moveTo(BatchedSortationOverrideStatus.DRAFT) }
    }
}
