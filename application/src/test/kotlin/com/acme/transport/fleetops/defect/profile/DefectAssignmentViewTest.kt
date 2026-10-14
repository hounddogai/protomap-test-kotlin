package com.acme.transport.fleetops.defect.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class DefectAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefectAssignment("code-957", "name-818")
        val view = toDefectAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefectAssignmentViews(mutableListOf<DefectAssignment>(item, item)).size, "views")
    }
}
