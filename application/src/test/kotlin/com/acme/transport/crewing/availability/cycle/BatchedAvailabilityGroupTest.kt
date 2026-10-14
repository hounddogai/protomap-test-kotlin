package com.acme.transport.crewing.availability.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedAvailabilityGroupTest {
    @Test
    fun `starts in draft`() {
        val item = newBatchedAvailabilityGroup("code-899", "label-255")
        assertEquals(BatchedAvailabilityGroupStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newBatchedAvailabilityGroup("code-899", "label-255")
        try {
            item.moveTo(BatchedAvailabilityGroupStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedAvailabilityGroupStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newBatchedAvailabilityGroup("code-899", "label-255")
        item.status = BatchedAvailabilityGroupStatus.CLOSED
        assertFails("move") { item.moveTo(BatchedAvailabilityGroupStatus.DRAFT) }
    }
}
