package com.acme.transport.network.transittime.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalTransitTimeEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalTransitTimeEntry("code-628", "label-682")
        val view = toSeasonalTransitTimeEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalTransitTimeEntryViews(mutableListOf<SeasonalTransitTimeEntry>(item, item)).size, "views")
    }
}
