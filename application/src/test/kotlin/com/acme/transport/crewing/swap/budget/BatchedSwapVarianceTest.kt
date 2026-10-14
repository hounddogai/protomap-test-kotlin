package com.acme.transport.crewing.swap.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedSwapVarianceTest {
    @Test
    fun `starts in open`() {
        val item = newBatchedSwapVariance("code-395", "name-538")
        assertEquals(BatchedSwapVarianceStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newBatchedSwapVariance("code-395", "name-538")
        try {
            item.moveTo(BatchedSwapVarianceStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedSwapVarianceStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newBatchedSwapVariance("code-395", "name-538")
        item.status = BatchedSwapVarianceStatus.ARCHIVED
        assertFails("move") { item.moveTo(BatchedSwapVarianceStatus.OPEN) }
    }
}
