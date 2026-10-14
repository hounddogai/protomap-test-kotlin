package com.acme.transport.crewing.swap.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientSwapBatchTest {
    @Test
    fun `starts in proposed`() {
        val item = newLenientSwapBatch("code-303", "name-455")
        assertEquals(LenientSwapBatchStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newLenientSwapBatch("code-303", "name-455")
        try {
            item.moveTo(LenientSwapBatchStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientSwapBatchStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLenientSwapBatch("code-303", "name-455")
        item.status = LenientSwapBatchStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LenientSwapBatchStatus.PROPOSED) }
    }
}
