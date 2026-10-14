package com.acme.transport.crewing.swap.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalSwapSegmentTest {
    @Test
    fun `starts in proposed`() {
        val item = newGlobalSwapSegment("code-396", "title-267")
        assertEquals(GlobalSwapSegmentStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newGlobalSwapSegment("code-396", "title-267")
        try {
            item.moveTo(GlobalSwapSegmentStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalSwapSegmentStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newGlobalSwapSegment("code-396", "title-267")
        item.status = GlobalSwapSegmentStatus.WITHDRAWN
        assertFails("move") { item.moveTo(GlobalSwapSegmentStatus.PROPOSED) }
    }
}
