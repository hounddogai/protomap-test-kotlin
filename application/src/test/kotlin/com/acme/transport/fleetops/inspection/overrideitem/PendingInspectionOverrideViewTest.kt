package com.acme.transport.fleetops.inspection.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingInspectionOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingInspectionOverride("code-55", "title-489")
        val view = toPendingInspectionOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingInspectionOverrideViews(mutableListOf<PendingInspectionOverride>(item, item)).size, "views")
    }
}
