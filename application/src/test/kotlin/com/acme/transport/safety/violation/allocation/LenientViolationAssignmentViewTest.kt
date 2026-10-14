package com.acme.transport.safety.violation.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientViolationAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientViolationAssignment("code-459", "label-834")
        val view = toLenientViolationAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientViolationAssignmentViews(mutableListOf<LenientViolationAssignment>(item, item)).size, "views")
    }
}
