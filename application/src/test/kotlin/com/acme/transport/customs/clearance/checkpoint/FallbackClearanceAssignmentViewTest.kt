package com.acme.transport.customs.clearance.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackClearanceAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackClearanceAssignment("code-746", "label-666")
        val view = toFallbackClearanceAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackClearanceAssignmentViews(mutableListOf<FallbackClearanceAssignment>(item, item)).size, "views")
    }
}
