package com.acme.transport.fleetops.inspection.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionNotice("code-661", "name-105")
        val view = toInspectionNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionNoticeViews(mutableListOf<InspectionNotice>(item, item)).size, "views")
    }
}
