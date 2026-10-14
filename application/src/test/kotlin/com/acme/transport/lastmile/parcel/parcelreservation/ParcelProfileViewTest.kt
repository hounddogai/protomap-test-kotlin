package com.acme.transport.lastmile.parcel.parcelreservation

import kotlin.test.Test
import kotlin.test.assertEquals

class ParcelProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newParcelProfile("code-179", "name-263")
        val view = toParcelProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toParcelProfileViews(mutableListOf<ParcelProfile>(item, item)).size, "views")
    }
}
