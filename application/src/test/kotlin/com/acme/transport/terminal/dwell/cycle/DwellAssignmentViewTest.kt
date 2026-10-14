package com.acme.transport.terminal.dwell.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class DwellAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDwellAssignment("code-828", "label-263")
        val view = toDwellAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDwellAssignmentViews(mutableListOf<DwellAssignment>(item, item)).size, "views")
    }
}
