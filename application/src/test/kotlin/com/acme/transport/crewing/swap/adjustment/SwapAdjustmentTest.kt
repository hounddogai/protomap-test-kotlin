package com.acme.transport.crewing.swap.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SwapAdjustmentTest {
    @Test
    fun `starts in new`() {
        val item = newSwapAdjustment("code-308", "label-845")
        assertEquals(SwapAdjustmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newSwapAdjustment("code-308", "label-845")
        try {
            item.moveTo(SwapAdjustmentStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SwapAdjustmentStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newSwapAdjustment("code-308", "label-845")
        item.status = SwapAdjustmentStatus.FAILED
        assertFails("move") { item.moveTo(SwapAdjustmentStatus.NEW) }
    }
}
