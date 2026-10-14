package com.acme.transport.network.zone.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyZoneScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyZoneScore("code-626", "title-46")
        val view = toLegacyZoneScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyZoneScoreViews(mutableListOf<LegacyZoneScore>(item, item)).size, "views")
    }
}
