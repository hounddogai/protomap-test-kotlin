package com.acme.transport.terminal.dockdoor.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedDockDoorMappingTest {
    @Test
    fun `starts in open`() {
        val item = newBatchedDockDoorMapping("code-846", "title-558")
        assertEquals(BatchedDockDoorMappingStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newBatchedDockDoorMapping("code-846", "title-558")
        try {
            item.moveTo(BatchedDockDoorMappingStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedDockDoorMappingStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newBatchedDockDoorMapping("code-846", "title-558")
        item.status = BatchedDockDoorMappingStatus.ARCHIVED
        assertFails("move") { item.moveTo(BatchedDockDoorMappingStatus.OPEN) }
    }
}
