package com.acme.transport.fleetops.tire.tireclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedTireThresholdTest {
    @Test
    fun `starts in new`() {
        val item = newBatchedTireThreshold("code-156", "title-930")
        assertEquals(BatchedTireThresholdStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newBatchedTireThreshold("code-156", "title-930")
        try {
            item.moveTo(BatchedTireThresholdStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedTireThresholdStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newBatchedTireThreshold("code-156", "title-930")
        item.status = BatchedTireThresholdStatus.FAILED
        assertFails("move") { item.moveTo(BatchedTireThresholdStatus.NEW) }
    }
}
