package com.acme.transport.linehaul.laneplan.item

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedLanePlanPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedLanePlanPreference("code-836", "title-761")
        val view = toCappedLanePlanPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedLanePlanPreferenceViews(mutableListOf<CappedLanePlanPreference>(item, item)).size, "views")
    }
}
