package com.acme.transport.crewing.roster.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class RosterAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRosterAdjustment("code-463", "name-347")
        val view = toRosterAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRosterAdjustmentViews(mutableListOf<RosterAdjustment>(item, item)).size, "views")
    }
}
