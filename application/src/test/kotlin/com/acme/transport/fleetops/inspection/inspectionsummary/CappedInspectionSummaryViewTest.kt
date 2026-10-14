package com.acme.transport.fleetops.inspection.inspectionsummary

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedInspectionSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedInspectionSummary("code-978", "title-696")
        val view = toCappedInspectionSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedInspectionSummaryViews(mutableListOf<CappedInspectionSummary>(item, item)).size, "views")
    }
}
