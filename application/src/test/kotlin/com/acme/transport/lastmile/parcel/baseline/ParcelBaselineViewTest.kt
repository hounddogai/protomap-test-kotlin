package com.acme.transport.lastmile.parcel.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class ParcelBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newParcelBaseline("code-641", "label-731")
        val view = toParcelBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toParcelBaselineViews(mutableListOf<ParcelBaseline>(item, item)).size, "views")
    }
}
