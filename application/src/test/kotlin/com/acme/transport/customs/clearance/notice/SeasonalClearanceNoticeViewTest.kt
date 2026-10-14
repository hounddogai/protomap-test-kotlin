package com.acme.transport.customs.clearance.notice

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalClearanceNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalClearanceNotice("code-635", "title-314")
        val view = toSeasonalClearanceNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalClearanceNoticeViews(mutableListOf<SeasonalClearanceNotice>(item, item)).size, "views")
    }
}
