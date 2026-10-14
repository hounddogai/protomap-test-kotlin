package com.acme.transport.fleetops.utilization.reservation

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryUtilizationAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryUtilizationAssignment("code-821", "title-968")
        val view = toPrimaryUtilizationAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryUtilizationAssignmentViews(mutableListOf<PrimaryUtilizationAssignment>(item, item)).size, "views")
    }
}
