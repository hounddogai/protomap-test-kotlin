package com.acme.transport.linehaul.laneplan.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledLanePlanOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledLanePlanOverride("code-90", "name-747")
        val view = toScheduledLanePlanOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledLanePlanOverrideViews(mutableListOf<ScheduledLanePlanOverride>(item, item)).size, "views")
    }
}
