package com.acme.transport.fleetops.defect.defectcheckpoint

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackDefectDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackDefectDraft("code-633", "label-131")
        val view = toFallbackDefectDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackDefectDraftViews(mutableListOf<FallbackDefectDraft>(item, item)).size, "views")
    }
}
