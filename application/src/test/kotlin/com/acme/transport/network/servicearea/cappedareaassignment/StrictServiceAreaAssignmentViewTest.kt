package com.acme.transport.network.servicearea.cappedareaassignment

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictServiceAreaAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictServiceAreaAssignment("code-748", "name-382")
        val view = toStrictServiceAreaAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictServiceAreaAssignmentViews(mutableListOf<StrictServiceAreaAssignment>(item, item)).size, "views")
    }
}
