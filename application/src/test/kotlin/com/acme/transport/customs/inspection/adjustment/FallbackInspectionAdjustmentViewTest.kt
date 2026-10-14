package com.acme.transport.customs.inspection.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackInspectionAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackInspectionAdjustment("code-293", "name-907")
        val view = toFallbackInspectionAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackInspectionAdjustmentViews(mutableListOf<FallbackInspectionAdjustment>(item, item)).size, "views")
    }
}
