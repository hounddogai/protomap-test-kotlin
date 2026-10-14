package com.acme.transport.linehaul.capacity.capacityassignment

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalCapacityProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalCapacityProfile("code-630", "name-469")
        val view = toRegionalCapacityProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalCapacityProfileViews(mutableListOf<RegionalCapacityProfile>(item, item)).size, "views")
    }
}
