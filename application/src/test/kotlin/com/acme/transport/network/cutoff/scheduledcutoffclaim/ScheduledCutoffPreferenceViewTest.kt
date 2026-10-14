package com.acme.transport.network.cutoff.scheduledcutoffclaim

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledCutoffPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledCutoffPreference("code-47", "title-346")
        val view = toScheduledCutoffPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledCutoffPreferenceViews(mutableListOf<ScheduledCutoffPreference>(item, item)).size, "views")
    }
}
