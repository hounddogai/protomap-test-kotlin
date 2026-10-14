package com.acme.transport.fleetops.tire.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyTireDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyTireDraft("code-219", "title-582")
        val view = toLegacyTireDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyTireDraftViews(mutableListOf<LegacyTireDraft>(item, item)).size, "views")
    }
}
