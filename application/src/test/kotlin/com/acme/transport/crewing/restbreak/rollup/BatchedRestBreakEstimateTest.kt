package com.acme.transport.crewing.restbreak.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedRestBreakEstimateTest {
    @Test
    fun `starts in open`() {
        val item = newBatchedRestBreakEstimate("code-702", "title-714")
        assertEquals(BatchedRestBreakEstimateStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newBatchedRestBreakEstimate("code-702", "title-714")
        try {
            item.moveTo(BatchedRestBreakEstimateStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedRestBreakEstimateStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newBatchedRestBreakEstimate("code-702", "title-714")
        item.status = BatchedRestBreakEstimateStatus.ARCHIVED
        assertFails("move") { item.moveTo(BatchedRestBreakEstimateStatus.OPEN) }
    }
}
