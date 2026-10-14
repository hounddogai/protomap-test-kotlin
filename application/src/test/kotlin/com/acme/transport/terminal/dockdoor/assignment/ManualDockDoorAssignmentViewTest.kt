package com.acme.transport.terminal.dockdoor.assignment

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualDockDoorAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualDockDoorAssignment("code-594", "title-356")
        val view = toManualDockDoorAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualDockDoorAssignmentViews(mutableListOf<ManualDockDoorAssignment>(item, item)).size, "views")
    }
}
