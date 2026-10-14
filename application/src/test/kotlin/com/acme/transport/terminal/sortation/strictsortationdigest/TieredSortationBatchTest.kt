package com.acme.transport.terminal.sortation.strictsortationdigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredSortationBatchTest {
    @Test
    fun `starts in requested`() {
        val item = newTieredSortationBatch("code-223", "name-950")
        assertEquals(TieredSortationBatchStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newTieredSortationBatch("code-223", "name-950")
        try {
            item.moveTo(TieredSortationBatchStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredSortationBatchStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTieredSortationBatch("code-223", "name-950")
        item.status = TieredSortationBatchStatus.EXPIRED
        assertFails("move") { item.moveTo(TieredSortationBatchStatus.REQUESTED) }
    }
}
