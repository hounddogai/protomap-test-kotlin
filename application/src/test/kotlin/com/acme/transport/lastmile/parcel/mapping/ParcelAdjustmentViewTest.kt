package com.acme.transport.lastmile.parcel.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class ParcelAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newParcelAdjustment("code-577", "name-495")
        val view = toParcelAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toParcelAdjustmentViews(mutableListOf<ParcelAdjustment>(item, item)).size, "views")
    }
}
