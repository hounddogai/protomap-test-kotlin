package com.acme.transport.terminal.dwell.dwelldigest

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledDwellAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledDwellAdjustment("code-138", "title-291")
        val view = toScheduledDwellAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledDwellAdjustmentViews(mutableListOf<ScheduledDwellAdjustment>(item, item)).size, "views")
    }
}
