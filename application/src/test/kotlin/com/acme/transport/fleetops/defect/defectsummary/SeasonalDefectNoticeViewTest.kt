package com.acme.transport.fleetops.defect.defectsummary

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalDefectNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalDefectNotice("code-96", "name-185")
        val view = toSeasonalDefectNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalDefectNoticeViews(mutableListOf<SeasonalDefectNotice>(item, item)).size, "views")
    }
}
