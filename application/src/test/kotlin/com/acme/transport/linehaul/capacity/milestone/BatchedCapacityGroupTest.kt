package com.acme.transport.linehaul.capacity.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedCapacityGroupTest {
    @Test
    fun `starts in open`() {
        val item = newBatchedCapacityGroup("code-554", "label-655")
        assertEquals(BatchedCapacityGroupStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newBatchedCapacityGroup("code-554", "label-655")
        try {
            item.moveTo(BatchedCapacityGroupStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedCapacityGroupStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newBatchedCapacityGroup("code-554", "label-655")
        item.status = BatchedCapacityGroupStatus.ARCHIVED
        assertFails("move") { item.moveTo(BatchedCapacityGroupStatus.OPEN) }
    }
}
