package com.acme.transport.crewing.swap.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SwapRollupTest {
    @Test
    fun `starts in draft`() {
        val item = newSwapRollup("code-134", "name-245")
        assertEquals(SwapRollupStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newSwapRollup("code-134", "name-245")
        try {
            item.moveTo(SwapRollupStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SwapRollupStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newSwapRollup("code-134", "name-245")
        item.status = SwapRollupStatus.CLOSED
        assertFails("move") { item.moveTo(SwapRollupStatus.DRAFT) }
    }
}
