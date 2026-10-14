package com.acme.transport.terminal.crossdock.cappeddockestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedCrossDockCycleTest {
    @Test
    fun `starts in idle`() {
        val item = newBatchedCrossDockCycle("code-984", "label-237")
        assertEquals(BatchedCrossDockCycleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newBatchedCrossDockCycle("code-984", "label-237")
        try {
            item.moveTo(BatchedCrossDockCycleStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedCrossDockCycleStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newBatchedCrossDockCycle("code-984", "label-237")
        item.status = BatchedCrossDockCycleStatus.STOPPED
        assertFails("move") { item.moveTo(BatchedCrossDockCycleStatus.IDLE) }
    }
}
