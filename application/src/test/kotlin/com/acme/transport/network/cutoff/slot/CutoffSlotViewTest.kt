package com.acme.transport.network.cutoff.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class CutoffSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCutoffSlot("code-538", "title-149")
        val view = toCutoffSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCutoffSlotViews(mutableListOf<CutoffSlot>(item, item)).size, "views")
    }
}
