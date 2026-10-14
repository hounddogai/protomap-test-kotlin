package com.acme.transport.network.zone.zonebundle

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyZoneBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyZoneBundle("code-506", "name-860")
        val view = toLegacyZoneBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyZoneBundleViews(mutableListOf<LegacyZoneBundle>(item, item)).size, "views")
    }
}
