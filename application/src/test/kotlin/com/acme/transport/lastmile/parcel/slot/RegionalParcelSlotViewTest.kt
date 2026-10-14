package com.acme.transport.lastmile.parcel.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalParcelSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalParcelSlot("code-830", "title-3")
        val view = toRegionalParcelSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalParcelSlotViews(mutableListOf<RegionalParcelSlot>(item, item)).size, "views")
    }
}
