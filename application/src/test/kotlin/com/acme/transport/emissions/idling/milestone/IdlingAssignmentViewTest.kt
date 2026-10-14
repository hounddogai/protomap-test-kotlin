package com.acme.transport.emissions.idling.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class IdlingAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newIdlingAssignment("code-401", "label-842")
        val view = toIdlingAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toIdlingAssignmentViews(mutableListOf<IdlingAssignment>(item, item)).size, "views")
    }
}
