package com.acme.transport.crewing.swap.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedSwapBundleTest {
    @Test
    fun `starts in proposed`() {
        val item = newBatchedSwapBundle("code-742", "name-905")
        assertEquals(BatchedSwapBundleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newBatchedSwapBundle("code-742", "name-905")
        try {
            item.moveTo(BatchedSwapBundleStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedSwapBundleStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newBatchedSwapBundle("code-742", "name-905")
        item.status = BatchedSwapBundleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(BatchedSwapBundleStatus.PROPOSED) }
    }
}
