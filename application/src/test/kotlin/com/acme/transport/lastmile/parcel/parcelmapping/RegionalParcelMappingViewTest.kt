package com.acme.transport.lastmile.parcel.parcelmapping

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalParcelMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalParcelMapping("code-180", "name-69")
        val view = toRegionalParcelMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalParcelMappingViews(mutableListOf<RegionalParcelMapping>(item, item)).size, "views")
    }
}
