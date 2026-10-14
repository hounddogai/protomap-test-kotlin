package com.acme.transport.terminal.sortation.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictSortationBatchTest {
    @Test
    fun `starts in proposed`() {
        val item = newStrictSortationBatch("code-811", "title-985")
        assertEquals(StrictSortationBatchStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newStrictSortationBatch("code-811", "title-985")
        try {
            item.moveTo(StrictSortationBatchStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictSortationBatchStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newStrictSortationBatch("code-811", "title-985")
        item.status = StrictSortationBatchStatus.WITHDRAWN
        assertFails("move") { item.moveTo(StrictSortationBatchStatus.PROPOSED) }
    }
}
