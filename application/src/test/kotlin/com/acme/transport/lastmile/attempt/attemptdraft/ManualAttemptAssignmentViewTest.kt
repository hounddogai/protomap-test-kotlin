package com.acme.transport.lastmile.attempt.attemptdraft

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualAttemptAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualAttemptAssignment("code-416", "title-508")
        val view = toManualAttemptAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualAttemptAssignmentViews(mutableListOf<ManualAttemptAssignment>(item, item)).size, "views")
    }
}
