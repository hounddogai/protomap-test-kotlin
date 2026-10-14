package com.acme.transport.terminal.dockdoor.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class DockDoorMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDockDoorMapping("code-439", "name-212")
        val view = toDockDoorMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDockDoorMappingViews(mutableListOf<DockDoorMapping>(item, item)).size, "views")
    }
}
