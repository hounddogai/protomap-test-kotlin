package com.acme.transport.terminal.dockdoor.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictDockDoorPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictDockDoorPlan("code-349", "label-255")
        val view = toStrictDockDoorPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictDockDoorPlanViews(mutableListOf<StrictDockDoorPlan>(item, item)).size, "views")
    }
}
