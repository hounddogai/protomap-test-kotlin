package com.acme.transport.lastmile.parcel.window

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalParcelWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalParcelWindow("code-771", "title-412")
        val view = toRegionalParcelWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalParcelWindowViews(mutableListOf<RegionalParcelWindow>(item, item)).size, "views")
    }
}
