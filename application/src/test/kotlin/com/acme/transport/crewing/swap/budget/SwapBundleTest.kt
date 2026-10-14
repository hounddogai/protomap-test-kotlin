package com.acme.transport.crewing.swap.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SwapBundleTest {
    @Test
    fun `starts in open`() {
        val item = newSwapBundle("code-194", "title-712")
        assertEquals(SwapBundleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newSwapBundle("code-194", "title-712")
        try {
            item.moveTo(SwapBundleStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SwapBundleStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newSwapBundle("code-194", "title-712")
        item.status = SwapBundleStatus.ARCHIVED
        assertFails("move") { item.moveTo(SwapBundleStatus.OPEN) }
    }
}
