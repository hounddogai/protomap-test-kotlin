package com.acme.transport.customs.clearance.notice

import kotlin.test.Test
import kotlin.test.assertEquals

class ClearanceAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newClearanceAdjustment("code-641", "title-255")
        val view = toClearanceAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toClearanceAdjustmentViews(mutableListOf<ClearanceAdjustment>(item, item)).size, "views")
    }
}
