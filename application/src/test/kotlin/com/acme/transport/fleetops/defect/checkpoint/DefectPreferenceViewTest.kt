package com.acme.transport.fleetops.defect.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals

class DefectPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefectPreference("code-917", "title-321")
        val view = toDefectPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefectPreferenceViews(mutableListOf<DefectPreference>(item, item)).size, "views")
    }
}
