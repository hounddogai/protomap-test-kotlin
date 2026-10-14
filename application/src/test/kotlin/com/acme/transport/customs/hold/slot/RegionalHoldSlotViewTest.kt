package com.acme.transport.customs.hold.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalHoldSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalHoldSlot("code-370", "name-427")
        val view = toRegionalHoldSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalHoldSlotViews(mutableListOf<RegionalHoldSlot>(item, item)).size, "views")
    }
}
