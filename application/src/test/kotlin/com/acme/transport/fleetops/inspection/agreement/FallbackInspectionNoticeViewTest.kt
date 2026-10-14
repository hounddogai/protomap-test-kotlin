package com.acme.transport.fleetops.inspection.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackInspectionNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackInspectionNotice("code-314", "name-877")
        val view = toFallbackInspectionNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackInspectionNoticeViews(mutableListOf<FallbackInspectionNotice>(item, item)).size, "views")
    }
}
