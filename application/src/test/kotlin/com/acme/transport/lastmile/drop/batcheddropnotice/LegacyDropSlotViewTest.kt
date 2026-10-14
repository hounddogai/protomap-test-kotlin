package com.acme.transport.lastmile.drop.batcheddropnotice

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyDropSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyDropSlot("code-564", "title-898")
        val view = toLegacyDropSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyDropSlotViews(mutableListOf<LegacyDropSlot>(item, item)).size, "views")
    }
}
