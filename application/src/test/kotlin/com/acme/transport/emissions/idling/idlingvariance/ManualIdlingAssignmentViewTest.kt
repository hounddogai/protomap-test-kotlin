package com.acme.transport.emissions.idling.idlingvariance

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualIdlingAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualIdlingAssignment("code-449", "title-798")
        val view = toManualIdlingAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualIdlingAssignmentViews(mutableListOf<ManualIdlingAssignment>(item, item)).size, "views")
    }
}
