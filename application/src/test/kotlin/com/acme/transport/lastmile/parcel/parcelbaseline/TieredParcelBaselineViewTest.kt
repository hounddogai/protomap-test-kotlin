package com.acme.transport.lastmile.parcel.parcelbaseline

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredParcelBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredParcelBaseline("code-104", "title-154")
        val view = toTieredParcelBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredParcelBaselineViews(mutableListOf<TieredParcelBaseline>(item, item)).size, "views")
    }
}
