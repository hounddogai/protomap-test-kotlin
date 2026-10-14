package com.acme.transport.terminal.dockdoor.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedDockDoorWindowTest {
    @Test
    fun `starts in open`() {
        val item = newBatchedDockDoorWindow("code-635", "name-410")
        assertEquals(BatchedDockDoorWindowStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newBatchedDockDoorWindow("code-635", "name-410")
        try {
            item.moveTo(BatchedDockDoorWindowStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedDockDoorWindowStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newBatchedDockDoorWindow("code-635", "name-410")
        item.status = BatchedDockDoorWindowStatus.ARCHIVED
        assertFails("move") { item.moveTo(BatchedDockDoorWindowStatus.OPEN) }
    }
}
