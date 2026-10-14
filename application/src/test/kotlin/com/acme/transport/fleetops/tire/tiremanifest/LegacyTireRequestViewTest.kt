package com.acme.transport.fleetops.tire.tiremanifest

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyTireRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyTireRequest("code-609", "name-689")
        val view = toLegacyTireRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyTireRequestViews(mutableListOf<LegacyTireRequest>(item, item)).size, "views")
    }
}
