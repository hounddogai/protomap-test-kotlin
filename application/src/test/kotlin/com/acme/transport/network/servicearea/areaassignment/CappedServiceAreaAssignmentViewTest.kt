package com.acme.transport.network.servicearea.areaassignment

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedServiceAreaAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedServiceAreaAssignment("code-118", "name-513")
        val view = toCappedServiceAreaAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedServiceAreaAssignmentViews(mutableListOf<CappedServiceAreaAssignment>(item, item)).size, "views")
    }
}
