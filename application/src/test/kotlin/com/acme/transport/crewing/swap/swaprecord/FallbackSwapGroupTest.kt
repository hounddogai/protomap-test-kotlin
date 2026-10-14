package com.acme.transport.crewing.swap.swaprecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackSwapGroupTest {
    @Test
    fun `starts in requested`() {
        val item = newFallbackSwapGroup("code-29", "label-535")
        assertEquals(FallbackSwapGroupStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newFallbackSwapGroup("code-29", "label-535")
        try {
            item.moveTo(FallbackSwapGroupStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackSwapGroupStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newFallbackSwapGroup("code-29", "label-535")
        item.status = FallbackSwapGroupStatus.EXPIRED
        assertFails("move") { item.moveTo(FallbackSwapGroupStatus.REQUESTED) }
    }
}
