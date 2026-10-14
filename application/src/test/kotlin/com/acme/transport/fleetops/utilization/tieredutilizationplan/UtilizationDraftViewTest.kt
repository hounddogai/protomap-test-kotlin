package com.acme.transport.fleetops.utilization.tieredutilizationplan

import kotlin.test.Test
import kotlin.test.assertEquals

class UtilizationDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newUtilizationDraft("code-818", "label-135")
        val view = toUtilizationDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toUtilizationDraftViews(mutableListOf<UtilizationDraft>(item, item)).size, "views")
    }
}
