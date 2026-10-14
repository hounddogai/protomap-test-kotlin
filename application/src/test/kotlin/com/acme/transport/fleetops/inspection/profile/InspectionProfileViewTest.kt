package com.acme.transport.fleetops.inspection.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionProfile("code-939", "title-162")
        val view = toInspectionProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionProfileViews(mutableListOf<InspectionProfile>(item, item)).size, "views")
    }
}
