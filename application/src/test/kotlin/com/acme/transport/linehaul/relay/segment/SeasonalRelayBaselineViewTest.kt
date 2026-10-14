package com.acme.transport.linehaul.relay.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalRelayBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalRelayBaseline("code-558", "name-698")
        val view = toSeasonalRelayBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalRelayBaselineViews(mutableListOf<SeasonalRelayBaseline>(item, item)).size, "views")
    }
}
