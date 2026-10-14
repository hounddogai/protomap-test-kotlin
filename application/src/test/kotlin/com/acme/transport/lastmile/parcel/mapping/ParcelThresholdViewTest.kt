package com.acme.transport.lastmile.parcel.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class ParcelThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newParcelThreshold("code-81", "label-988")
        val view = toParcelThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toParcelThresholdViews(mutableListOf<ParcelThreshold>(item, item)).size, "views")
    }
}
