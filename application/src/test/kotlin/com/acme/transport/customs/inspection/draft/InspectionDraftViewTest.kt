package com.acme.transport.customs.inspection.draft

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionDraft("code-966", "name-452")
        val view = toInspectionDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionDraftViews(mutableListOf<InspectionDraft>(item, item)).size, "views")
    }
}
