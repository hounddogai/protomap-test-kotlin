package com.acme.transport.fleetops.inspection.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalInspectionDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalInspectionDraft("code-486", "title-711")
        val view = toSeasonalInspectionDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalInspectionDraftViews(mutableListOf<SeasonalInspectionDraft>(item, item)).size, "views")
    }
}
