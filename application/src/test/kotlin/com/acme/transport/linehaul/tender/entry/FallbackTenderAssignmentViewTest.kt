package com.acme.transport.linehaul.tender.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackTenderAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackTenderAssignment("code-741", "label-292")
        val view = toFallbackTenderAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackTenderAssignmentViews(mutableListOf<FallbackTenderAssignment>(item, item)).size, "views")
    }
}
