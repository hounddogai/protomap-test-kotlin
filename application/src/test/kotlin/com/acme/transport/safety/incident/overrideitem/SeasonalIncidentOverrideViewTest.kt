package com.acme.transport.safety.incident.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalIncidentOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalIncidentOverride("code-795", "title-693")
        val view = toSeasonalIncidentOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalIncidentOverrideViews(mutableListOf<SeasonalIncidentOverride>(item, item)).size, "views")
    }
}
