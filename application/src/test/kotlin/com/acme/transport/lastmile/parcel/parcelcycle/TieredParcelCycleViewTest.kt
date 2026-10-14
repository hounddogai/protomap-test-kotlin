package com.acme.transport.lastmile.parcel.parcelcycle

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredParcelCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredParcelCycle("code-510", "title-188")
        val view = toTieredParcelCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredParcelCycleViews(mutableListOf<TieredParcelCycle>(item, item)).size, "views")
    }
}
