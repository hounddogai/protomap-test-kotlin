package com.acme.transport.lastmile.attempt.attemptplan

import kotlin.test.Test
import kotlin.test.assertEquals

class AttemptAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAttemptAssignment("code-154", "title-509")
        val view = toAttemptAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAttemptAssignmentViews(mutableListOf<AttemptAssignment>(item, item)).size, "views")
    }
}
