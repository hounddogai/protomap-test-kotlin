package com.acme.transport.linehaul.laneplan.planthreshold

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingLanePlanDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingLanePlanDraft("code-364", "name-819")
        val view = toRollingLanePlanDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingLanePlanDraftViews(mutableListOf<RollingLanePlanDraft>(item, item)).size, "views")
    }
}
