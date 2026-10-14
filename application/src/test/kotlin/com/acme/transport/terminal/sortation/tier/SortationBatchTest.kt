package com.acme.transport.terminal.sortation.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SortationBatchTest {
    @Test
    fun `starts in requested`() {
        val item = newSortationBatch("code-799", "name-564")
        assertEquals(SortationBatchStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newSortationBatch("code-799", "name-564")
        try {
            item.moveTo(SortationBatchStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SortationBatchStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newSortationBatch("code-799", "name-564")
        item.status = SortationBatchStatus.EXPIRED
        assertFails("move") { item.moveTo(SortationBatchStatus.REQUESTED) }
    }
}
