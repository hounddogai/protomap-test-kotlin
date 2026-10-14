package com.acme.transport.customs.clearance.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualClearanceCheckpointTest {
    @Test
    fun `starts in draft`() {
        val item = newManualClearanceCheckpoint("code-381", "name-723")
        assertEquals(ManualClearanceCheckpointStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newManualClearanceCheckpoint("code-381", "name-723")
        try {
            item.moveTo(ManualClearanceCheckpointStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualClearanceCheckpointStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newManualClearanceCheckpoint("code-381", "name-723")
        item.status = ManualClearanceCheckpointStatus.CLOSED
        assertFails("move") { item.moveTo(ManualClearanceCheckpointStatus.DRAFT) }
    }
}
