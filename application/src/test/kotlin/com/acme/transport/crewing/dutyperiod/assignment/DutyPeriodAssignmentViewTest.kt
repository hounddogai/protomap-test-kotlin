package com.acme.transport.crewing.dutyperiod.assignment

import kotlin.test.Test
import kotlin.test.assertEquals

class DutyPeriodAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDutyPeriodAssignment("code-991", "label-930")
        val view = toDutyPeriodAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDutyPeriodAssignmentViews(mutableListOf<DutyPeriodAssignment>(item, item)).size, "views")
    }
}
