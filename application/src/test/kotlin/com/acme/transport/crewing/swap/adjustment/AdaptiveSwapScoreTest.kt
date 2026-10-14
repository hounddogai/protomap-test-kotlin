package com.acme.transport.crewing.swap.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveSwapScoreTest {
    @Test
    fun `starts in draft`() {
        val item = newAdaptiveSwapScore("code-18", "label-991")
        assertEquals(AdaptiveSwapScoreStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newAdaptiveSwapScore("code-18", "label-991")
        try {
            item.moveTo(AdaptiveSwapScoreStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveSwapScoreStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAdaptiveSwapScore("code-18", "label-991")
        item.status = AdaptiveSwapScoreStatus.CLOSED
        assertFails("move") { item.moveTo(AdaptiveSwapScoreStatus.DRAFT) }
    }
}
