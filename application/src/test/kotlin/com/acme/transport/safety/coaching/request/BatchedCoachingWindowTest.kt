package com.acme.transport.safety.coaching.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedCoachingWindowTest {
    @Test
    fun `starts in idle`() {
        val item = newBatchedCoachingWindow("code-354", "label-852")
        assertEquals(BatchedCoachingWindowStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newBatchedCoachingWindow("code-354", "label-852")
        try {
            item.moveTo(BatchedCoachingWindowStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedCoachingWindowStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newBatchedCoachingWindow("code-354", "label-852")
        item.status = BatchedCoachingWindowStatus.STOPPED
        assertFails("move") { item.moveTo(BatchedCoachingWindowStatus.IDLE) }
    }
}
