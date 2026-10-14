package com.acme.transport.customs.inspection.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedInspectionSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedInspectionSlot("code-991", "name-144")
        val view = toCappedInspectionSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedInspectionSlotViews(mutableListOf<CappedInspectionSlot>(item, item)).size, "views")
    }
}
