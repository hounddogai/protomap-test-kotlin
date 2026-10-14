package com.acme.transport.customs.inspection.pendinginspectionrule

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionAssignment("code-374", "label-545")
        val view = toInspectionAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionAssignmentViews(mutableListOf<InspectionAssignment>(item, item)).size, "views")
    }
}
