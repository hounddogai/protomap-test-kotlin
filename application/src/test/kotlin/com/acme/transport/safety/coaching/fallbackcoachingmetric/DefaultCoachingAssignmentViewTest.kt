package com.acme.transport.safety.coaching.fallbackcoachingmetric

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultCoachingAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultCoachingAssignment("code-851", "label-562")
        val view = toDefaultCoachingAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultCoachingAssignmentViews(mutableListOf<DefaultCoachingAssignment>(item, item)).size, "views")
    }
}
