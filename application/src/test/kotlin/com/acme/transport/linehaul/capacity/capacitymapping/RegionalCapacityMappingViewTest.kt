package com.acme.transport.linehaul.capacity.capacitymapping

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalCapacityMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalCapacityMapping("code-185", "label-354")
        val view = toRegionalCapacityMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalCapacityMappingViews(mutableListOf<RegionalCapacityMapping>(item, item)).size, "views")
    }
}
