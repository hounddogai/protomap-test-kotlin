package com.acme.transport.crewing.dutyperiod.threshold

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalDutyPeriodAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalDutyPeriodAssignment("code-793", "name-974")
        val view = toSeasonalDutyPeriodAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalDutyPeriodAssignmentViews(mutableListOf<SeasonalDutyPeriodAssignment>(item, item)).size, "views")
    }
}
