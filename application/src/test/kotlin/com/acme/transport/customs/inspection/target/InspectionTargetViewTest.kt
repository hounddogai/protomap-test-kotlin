package com.acme.transport.customs.inspection.target

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionTarget("code-375", "title-707")
        val view = toInspectionTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionTargetViews(mutableListOf<InspectionTarget>(item, item)).size, "views")
    }
}
