package com.acme.transport.fleetops.utilization.reservation

import kotlin.test.Test
import kotlin.test.assertEquals

class UtilizationAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newUtilizationAssignment("code-97", "title-135")
        val view = toUtilizationAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toUtilizationAssignmentViews(mutableListOf<UtilizationAssignment>(item, item)).size, "views")
    }
}
