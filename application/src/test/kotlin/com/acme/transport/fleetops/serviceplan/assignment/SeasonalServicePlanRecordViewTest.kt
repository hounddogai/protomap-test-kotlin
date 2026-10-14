package com.acme.transport.fleetops.serviceplan.assignment

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalServicePlanRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalServicePlanRecord("code-214", "name-159")
        val view = toSeasonalServicePlanRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalServicePlanRecordViews(mutableListOf<SeasonalServicePlanRecord>(item, item)).size, "views")
    }
}
