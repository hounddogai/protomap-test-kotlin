package com.acme.transport.customs.inspection.inspectionoverride

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveInspectionDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveInspectionDraft("code-722", "name-575")
        val view = toAdaptiveInspectionDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveInspectionDraftViews(mutableListOf<AdaptiveInspectionDraft>(item, item)).size, "views")
    }
}
