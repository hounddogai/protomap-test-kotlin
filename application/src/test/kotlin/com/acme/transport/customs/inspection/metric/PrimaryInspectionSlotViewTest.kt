package com.acme.transport.customs.inspection.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryInspectionSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryInspectionSlot("code-41", "label-926")
        val view = toPrimaryInspectionSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryInspectionSlotViews(mutableListOf<PrimaryInspectionSlot>(item, item)).size, "views")
    }
}
