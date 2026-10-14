package com.acme.transport.crewing.swap.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SwapBatchTest {
    @Test
    fun `starts in draft`() {
        val item = newSwapBatch("code-600", "label-506")
        assertEquals(SwapBatchStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newSwapBatch("code-600", "label-506")
        try {
            item.moveTo(SwapBatchStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SwapBatchStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newSwapBatch("code-600", "label-506")
        item.status = SwapBatchStatus.CLOSED
        assertFails("move") { item.moveTo(SwapBatchStatus.DRAFT) }
    }
}
