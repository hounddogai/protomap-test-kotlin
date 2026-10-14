package com.acme.transport.network.zone.draft

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyZoneTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyZoneTier("code-810", "name-716")
        val view = toLegacyZoneTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyZoneTierViews(mutableListOf<LegacyZoneTier>(item, item)).size, "views")
    }
}
