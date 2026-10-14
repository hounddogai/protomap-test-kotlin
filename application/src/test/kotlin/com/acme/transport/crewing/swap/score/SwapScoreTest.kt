package com.acme.transport.crewing.swap.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SwapScoreTest {
    @Test
    fun `starts in open`() {
        val item = newSwapScore("code-195", "name-667")
        assertEquals(SwapScoreStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newSwapScore("code-195", "name-667")
        try {
            item.moveTo(SwapScoreStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SwapScoreStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newSwapScore("code-195", "name-667")
        item.status = SwapScoreStatus.ARCHIVED
        assertFails("move") { item.moveTo(SwapScoreStatus.OPEN) }
    }
}
