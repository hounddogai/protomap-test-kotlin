package com.acme.transport.terminal.dockdoor.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class DockDoorEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDockDoorEstimate("code-929", "title-341")
        val view = toDockDoorEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDockDoorEstimateViews(mutableListOf<DockDoorEstimate>(item, item)).size, "views")
    }
}
