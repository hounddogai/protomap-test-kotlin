package com.acme.transport.emissions.evcharge.assignment

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledEvChargeAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledEvChargeAssignment("code-157", "label-516")
        val view = toScheduledEvChargeAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledEvChargeAssignmentViews(mutableListOf<ScheduledEvChargeAssignment>(item, item)).size, "views")
    }
}
