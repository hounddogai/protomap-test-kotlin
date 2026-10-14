package com.acme.transport.crewing.swap.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredSwapSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredSwapSlot("code-475", "label-746")
        val view = toTieredSwapSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredSwapSlotViews(mutableListOf<TieredSwapSlot>(item, item)).size, "views")
    }
}
