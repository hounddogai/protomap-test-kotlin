package com.acme.transport.linehaul.backhaul.backhaulassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedBackhaulAssignmentTest {
    @Test
    fun `starts in draft`() {
        val item = newBatchedBackhaulAssignment("code-660", "title-594")
        assertEquals(BatchedBackhaulAssignmentStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newBatchedBackhaulAssignment("code-660", "title-594")
        try {
            item.moveTo(BatchedBackhaulAssignmentStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedBackhaulAssignmentStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newBatchedBackhaulAssignment("code-660", "title-594")
        item.status = BatchedBackhaulAssignmentStatus.CLOSED
        assertFails("move") { item.moveTo(BatchedBackhaulAssignmentStatus.DRAFT) }
    }
}
