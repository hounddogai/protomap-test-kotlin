package com.acme.transport.linehaul.laneplan.target

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyLanePlanEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyLanePlanEntry("code-867", "label-977")
        val view = toLegacyLanePlanEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyLanePlanEntryViews(mutableListOf<LegacyLanePlanEntry>(item, item)).size, "views")
    }
}
