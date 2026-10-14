package com.acme.transport.fleetops.utilization.assignment

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientUtilizationAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientUtilizationAssignment("code-931", "label-187")
        val view = toLenientUtilizationAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientUtilizationAssignmentViews(mutableListOf<LenientUtilizationAssignment>(item, item)).size, "views")
    }
}
