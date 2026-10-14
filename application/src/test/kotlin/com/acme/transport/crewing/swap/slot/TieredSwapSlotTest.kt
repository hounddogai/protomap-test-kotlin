package com.acme.transport.crewing.swap.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredSwapSlotTest {
    @Test
    fun `starts in requested`() {
        val item = newTieredSwapSlot("code-475", "label-746")
        assertEquals(TieredSwapSlotStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newTieredSwapSlot("code-475", "label-746")
        try {
            item.moveTo(TieredSwapSlotStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredSwapSlotStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTieredSwapSlot("code-475", "label-746")
        item.status = TieredSwapSlotStatus.EXPIRED
        assertFails("move") { item.moveTo(TieredSwapSlotStatus.REQUESTED) }
    }
}
