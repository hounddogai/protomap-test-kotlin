package com.acme.transport.safety.coaching.assignment

import kotlin.test.Test
import kotlin.test.assertEquals

class CoachingAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCoachingAssignment("code-700", "title-429")
        val view = toCoachingAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCoachingAssignmentViews(mutableListOf<CoachingAssignment>(item, item)).size, "views")
    }
}
