package com.acme.transport.linehaul.laneplan.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class LanePlanEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLanePlanEntry("code-874", "name-632")
        val view = toLanePlanEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLanePlanEntryViews(mutableListOf<LanePlanEntry>(item, item)).size, "views")
    }
}
