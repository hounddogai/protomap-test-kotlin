package com.acme.transport.network.hub.rollinghubentry

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalHubEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalHubEntry("code-986", "name-172")
        val view = toSeasonalHubEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalHubEntryViews(mutableListOf<SeasonalHubEntry>(item, item)).size, "views")
    }
}
