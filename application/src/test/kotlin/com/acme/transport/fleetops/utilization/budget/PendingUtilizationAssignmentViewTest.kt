package com.acme.transport.fleetops.utilization.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingUtilizationAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingUtilizationAssignment("code-540", "label-145")
        val view = toPendingUtilizationAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingUtilizationAssignmentViews(mutableListOf<PendingUtilizationAssignment>(item, item)).size, "views")
    }
}
