package com.acme.transport.crewing.swap.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SwapGroupTest {
    @Test
    fun `starts in open`() {
        val item = newSwapGroup("code-56", "title-695")
        assertEquals(SwapGroupStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newSwapGroup("code-56", "title-695")
        try {
            item.moveTo(SwapGroupStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SwapGroupStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newSwapGroup("code-56", "title-695")
        item.status = SwapGroupStatus.ARCHIVED
        assertFails("move") { item.moveTo(SwapGroupStatus.OPEN) }
    }
}
