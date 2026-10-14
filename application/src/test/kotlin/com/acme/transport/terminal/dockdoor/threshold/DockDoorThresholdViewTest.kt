package com.acme.transport.terminal.dockdoor.threshold

import kotlin.test.Test
import kotlin.test.assertEquals

class DockDoorThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDockDoorThreshold("code-993", "name-14")
        val view = toDockDoorThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDockDoorThresholdViews(mutableListOf<DockDoorThreshold>(item, item)).size, "views")
    }
}
