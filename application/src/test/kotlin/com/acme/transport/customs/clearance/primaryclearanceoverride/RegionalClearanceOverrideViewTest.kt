package com.acme.transport.customs.clearance.primaryclearanceoverride

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalClearanceOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalClearanceOverride("code-111", "label-610")
        val view = toRegionalClearanceOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalClearanceOverrideViews(mutableListOf<RegionalClearanceOverride>(item, item)).size, "views")
    }
}
