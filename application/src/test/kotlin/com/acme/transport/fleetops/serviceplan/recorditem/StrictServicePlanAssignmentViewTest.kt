package com.acme.transport.fleetops.serviceplan.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictServicePlanAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictServicePlanAssignment("code-973", "title-474")
        val view = toStrictServicePlanAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictServicePlanAssignmentViews(mutableListOf<StrictServicePlanAssignment>(item, item)).size, "views")
    }
}
