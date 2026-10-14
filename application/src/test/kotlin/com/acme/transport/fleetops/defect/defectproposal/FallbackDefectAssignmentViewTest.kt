package com.acme.transport.fleetops.defect.defectproposal

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackDefectAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackDefectAssignment("code-211", "name-472")
        val view = toFallbackDefectAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackDefectAssignmentViews(mutableListOf<FallbackDefectAssignment>(item, item)).size, "views")
    }
}
