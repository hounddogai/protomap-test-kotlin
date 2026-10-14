package com.acme.transport.network.zone.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyZoneOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyZoneOverride("code-180", "title-564")
        val view = toLegacyZoneOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyZoneOverrideViews(mutableListOf<LegacyZoneOverride>(item, item)).size, "views")
    }
}
