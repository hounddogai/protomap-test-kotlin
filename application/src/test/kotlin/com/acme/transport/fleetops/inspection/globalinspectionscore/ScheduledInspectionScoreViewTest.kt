package com.acme.transport.fleetops.inspection.globalinspectionscore

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledInspectionScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledInspectionScore("code-572", "name-691")
        val view = toScheduledInspectionScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledInspectionScoreViews(mutableListOf<ScheduledInspectionScore>(item, item)).size, "views")
    }
}
