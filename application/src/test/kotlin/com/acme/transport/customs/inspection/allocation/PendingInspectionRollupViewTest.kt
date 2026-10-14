package com.acme.transport.customs.inspection.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingInspectionRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingInspectionRollup("code-527", "label-321")
        val view = toPendingInspectionRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingInspectionRollupViews(mutableListOf<PendingInspectionRollup>(item, item)).size, "views")
    }
}
