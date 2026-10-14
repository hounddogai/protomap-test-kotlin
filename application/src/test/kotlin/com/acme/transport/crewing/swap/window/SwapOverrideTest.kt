package com.acme.transport.crewing.swap.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SwapOverrideTest {
    @Test
    fun `starts in requested`() {
        val item = newSwapOverride("code-944", "name-746")
        assertEquals(SwapOverrideStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newSwapOverride("code-944", "name-746")
        try {
            item.moveTo(SwapOverrideStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SwapOverrideStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newSwapOverride("code-944", "name-746")
        item.status = SwapOverrideStatus.EXPIRED
        assertFails("move") { item.moveTo(SwapOverrideStatus.REQUESTED) }
    }
}
