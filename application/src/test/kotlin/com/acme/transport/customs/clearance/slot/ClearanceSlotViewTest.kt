package com.acme.transport.customs.clearance.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class ClearanceSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newClearanceSlot("code-410", "name-69")
        val view = toClearanceSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toClearanceSlotViews(mutableListOf<ClearanceSlot>(item, item)).size, "views")
    }
}
