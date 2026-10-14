package com.acme.transport.linehaul.laneplan.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledLanePlanDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledLanePlanDraft("code-298", "title-322")
        val view = toScheduledLanePlanDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledLanePlanDraftViews(mutableListOf<ScheduledLanePlanDraft>(item, item)).size, "views")
    }
}
