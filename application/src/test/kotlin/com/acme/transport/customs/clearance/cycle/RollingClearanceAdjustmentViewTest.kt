package com.acme.transport.customs.clearance.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingClearanceAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingClearanceAdjustment("code-877", "title-309")
        val view = toRollingClearanceAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingClearanceAdjustmentViews(mutableListOf<RollingClearanceAdjustment>(item, item)).size, "views")
    }
}
