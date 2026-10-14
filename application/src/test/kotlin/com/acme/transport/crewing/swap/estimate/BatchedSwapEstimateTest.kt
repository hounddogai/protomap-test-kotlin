package com.acme.transport.crewing.swap.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedSwapEstimateTest {
    @Test
    fun `starts in open`() {
        val item = newBatchedSwapEstimate("code-454", "label-682")
        assertEquals(BatchedSwapEstimateStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newBatchedSwapEstimate("code-454", "label-682")
        try {
            item.moveTo(BatchedSwapEstimateStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedSwapEstimateStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newBatchedSwapEstimate("code-454", "label-682")
        item.status = BatchedSwapEstimateStatus.ARCHIVED
        assertFails("move") { item.moveTo(BatchedSwapEstimateStatus.OPEN) }
    }
}
