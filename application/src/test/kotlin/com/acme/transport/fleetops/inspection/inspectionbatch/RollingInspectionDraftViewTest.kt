package com.acme.transport.fleetops.inspection.inspectionbatch

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingInspectionDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingInspectionDraft("code-912", "title-550")
        val view = toRollingInspectionDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingInspectionDraftViews(mutableListOf<RollingInspectionDraft>(item, item)).size, "views")
    }
}
