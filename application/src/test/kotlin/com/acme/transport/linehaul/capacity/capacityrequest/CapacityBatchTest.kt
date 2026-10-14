package com.acme.transport.linehaul.capacity.capacityrequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CapacityBatchTest {
    @Test
    fun `starts in new`() {
        val item = newCapacityBatch("code-273", "title-45")
        assertEquals(CapacityBatchStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newCapacityBatch("code-273", "title-45")
        try {
            item.moveTo(CapacityBatchStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CapacityBatchStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCapacityBatch("code-273", "title-45")
        item.status = CapacityBatchStatus.FAILED
        assertFails("move") { item.moveTo(CapacityBatchStatus.NEW) }
    }
}
