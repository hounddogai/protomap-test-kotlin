package com.acme.transport.fleetops.defect.defectagreement

import kotlin.test.Test
import kotlin.test.assertEquals

class DefectBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefectBudget("code-360", "label-649")
        val view = toDefectBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefectBudgetViews(mutableListOf<DefectBudget>(item, item)).size, "views")
    }
}
