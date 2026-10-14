package com.acme.transport.fleetops.tire.tireclaim

import kotlin.test.Test
import kotlin.test.assertEquals

class TireAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTireAssignment("code-35", "title-403")
        val view = toTireAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTireAssignmentViews(mutableListOf<TireAssignment>(item, item)).size, "views")
    }
}
