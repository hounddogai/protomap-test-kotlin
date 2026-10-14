package com.acme.transport.fleetops.defect.defectsummary

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultDefectSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultDefectSummary("code-860", "label-927")
        val view = toDefaultDefectSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultDefectSummaryViews(mutableListOf<DefaultDefectSummary>(item, item)).size, "views")
    }
}
