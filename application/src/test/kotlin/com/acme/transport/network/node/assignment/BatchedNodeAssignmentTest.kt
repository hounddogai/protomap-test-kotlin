package com.acme.transport.network.node.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedNodeAssignmentTest {
    @Test
    fun `starts in created`() {
        val item = newBatchedNodeAssignment("code-833", "title-20")
        assertEquals(BatchedNodeAssignmentStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newBatchedNodeAssignment("code-833", "title-20")
        try {
            item.moveTo(BatchedNodeAssignmentStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedNodeAssignmentStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newBatchedNodeAssignment("code-833", "title-20")
        item.status = BatchedNodeAssignmentStatus.RETIRED
        assertFails("move") { item.moveTo(BatchedNodeAssignmentStatus.CREATED) }
    }
}
