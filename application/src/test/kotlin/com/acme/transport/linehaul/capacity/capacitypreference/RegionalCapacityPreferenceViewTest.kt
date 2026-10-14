package com.acme.transport.linehaul.capacity.capacitypreference

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalCapacityPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalCapacityPreference("code-994", "name-232")
        val view = toRegionalCapacityPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalCapacityPreferenceViews(mutableListOf<RegionalCapacityPreference>(item, item)).size, "views")
    }
}
