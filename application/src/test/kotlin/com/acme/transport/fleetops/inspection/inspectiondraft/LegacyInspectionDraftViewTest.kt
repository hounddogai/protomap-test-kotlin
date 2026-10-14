package com.acme.transport.fleetops.inspection.inspectiondraft

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyInspectionDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyInspectionDraft("code-637", "label-427")
        val view = toLegacyInspectionDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyInspectionDraftViews(mutableListOf<LegacyInspectionDraft>(item, item)).size, "views")
    }
}
