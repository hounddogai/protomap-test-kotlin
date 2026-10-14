package com.acme.transport.crewing.dutyperiod.assignment

import kotlin.test.Test
import kotlin.test.assertEquals

class DutyPeriodRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDutyPeriodRollup("code-386", "label-953")
        val view = toDutyPeriodRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDutyPeriodRollupViews(mutableListOf<DutyPeriodRollup>(item, item)).size, "views")
    }
}
