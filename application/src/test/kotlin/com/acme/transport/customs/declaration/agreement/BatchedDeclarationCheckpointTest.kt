package com.acme.transport.customs.declaration.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedDeclarationCheckpointTest {
    @Test
    fun `starts in planned`() {
        val item = newBatchedDeclarationCheckpoint("code-817", "name-806")
        assertEquals(BatchedDeclarationCheckpointStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newBatchedDeclarationCheckpoint("code-817", "name-806")
        try {
            item.moveTo(BatchedDeclarationCheckpointStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedDeclarationCheckpointStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newBatchedDeclarationCheckpoint("code-817", "name-806")
        item.status = BatchedDeclarationCheckpointStatus.CANCELLED
        assertFails("move") { item.moveTo(BatchedDeclarationCheckpointStatus.PLANNED) }
    }
}
