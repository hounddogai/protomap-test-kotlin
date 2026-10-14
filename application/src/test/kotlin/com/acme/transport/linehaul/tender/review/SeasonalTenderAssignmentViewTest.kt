package com.acme.transport.linehaul.tender.review

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalTenderAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalTenderAssignment("code-984", "label-263")
        val view = toSeasonalTenderAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalTenderAssignmentViews(mutableListOf<SeasonalTenderAssignment>(item, item)).size, "views")
    }
}
