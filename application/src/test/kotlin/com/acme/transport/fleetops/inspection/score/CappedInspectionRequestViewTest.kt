package com.acme.transport.fleetops.inspection.score

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedInspectionRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedInspectionRequest("code-305", "label-933")
        val view = toCappedInspectionRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedInspectionRequestViews(mutableListOf<CappedInspectionRequest>(item, item)).size, "views")
    }
}
