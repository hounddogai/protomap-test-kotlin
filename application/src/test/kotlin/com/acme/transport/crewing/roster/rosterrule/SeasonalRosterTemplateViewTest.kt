package com.acme.transport.crewing.roster.rosterrule

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalRosterTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalRosterTemplate("code-218", "label-14")
        val view = toSeasonalRosterTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalRosterTemplateViews(mutableListOf<SeasonalRosterTemplate>(item, item)).size, "views")
    }
}
