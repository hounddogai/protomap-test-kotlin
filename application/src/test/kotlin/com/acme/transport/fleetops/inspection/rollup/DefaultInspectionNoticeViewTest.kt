package com.acme.transport.fleetops.inspection.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultInspectionNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultInspectionNotice("code-288", "label-898")
        val view = toDefaultInspectionNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultInspectionNoticeViews(mutableListOf<DefaultInspectionNotice>(item, item)).size, "views")
    }
}
