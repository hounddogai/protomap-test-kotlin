package com.acme.transport.crewing.swap.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SwapSegmentTest {
    @Test
    fun `starts in draft`() {
        val item = newSwapSegment("code-148", "title-231")
        assertEquals(SwapSegmentStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newSwapSegment("code-148", "title-231")
        try {
            item.moveTo(SwapSegmentStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SwapSegmentStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newSwapSegment("code-148", "title-231")
        item.status = SwapSegmentStatus.CLOSED
        assertFails("move") { item.moveTo(SwapSegmentStatus.DRAFT) }
    }
}
