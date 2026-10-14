package com.acme.transport.terminal.dwell.assignment

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingDwellAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingDwellAssignment("code-850", "title-533")
        val view = toPendingDwellAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingDwellAssignmentViews(mutableListOf<PendingDwellAssignment>(item, item)).size, "views")
    }
}
