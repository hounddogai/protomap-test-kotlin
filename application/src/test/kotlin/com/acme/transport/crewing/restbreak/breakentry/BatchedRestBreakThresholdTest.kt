package com.acme.transport.crewing.restbreak.breakentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedRestBreakThresholdTest {
    @Test
    fun `starts in new`() {
        val item = newBatchedRestBreakThreshold("code-581", "label-395")
        assertEquals(BatchedRestBreakThresholdStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newBatchedRestBreakThreshold("code-581", "label-395")
        try {
            item.moveTo(BatchedRestBreakThresholdStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedRestBreakThresholdStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newBatchedRestBreakThreshold("code-581", "label-395")
        item.status = BatchedRestBreakThresholdStatus.FAILED
        assertFails("move") { item.moveTo(BatchedRestBreakThresholdStatus.NEW) }
    }
}
