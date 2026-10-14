package com.acme.transport.terminal.dockdoor.dooragreement

import kotlin.test.Test
import kotlin.test.assertEquals

class DockDoorAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDockDoorAssignment("code-312", "name-469")
        val view = toDockDoorAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDockDoorAssignmentViews(mutableListOf<DockDoorAssignment>(item, item)).size, "views")
    }
}
