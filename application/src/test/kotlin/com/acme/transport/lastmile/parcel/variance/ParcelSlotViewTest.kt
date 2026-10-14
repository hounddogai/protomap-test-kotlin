package com.acme.transport.lastmile.parcel.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class ParcelSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newParcelSlot("code-181", "label-433")
        val view = toParcelSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toParcelSlotViews(mutableListOf<ParcelSlot>(item, item)).size, "views")
    }
}
