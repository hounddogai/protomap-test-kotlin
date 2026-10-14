package com.acme.transport.crewing.swap.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SwapSlotTest {
    @Test
    fun `starts in open`() {
        val item = newSwapSlot("code-699", "title-413")
        assertEquals(SwapSlotStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newSwapSlot("code-699", "title-413")
        try {
            item.moveTo(SwapSlotStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SwapSlotStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newSwapSlot("code-699", "title-413")
        item.status = SwapSlotStatus.ARCHIVED
        assertFails("move") { item.moveTo(SwapSlotStatus.OPEN) }
    }
}
