package com.acme.transport.linehaul.laneplan.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class LanePlanPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLanePlanPreference("code-738", "label-431")
        val view = toLanePlanPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLanePlanPreferenceViews(mutableListOf<LanePlanPreference>(item, item)).size, "views")
    }
}
