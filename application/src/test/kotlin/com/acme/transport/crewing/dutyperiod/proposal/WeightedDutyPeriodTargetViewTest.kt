package com.acme.transport.crewing.dutyperiod.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedDutyPeriodTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedDutyPeriodTarget("code-82", "name-179")
        val view = toWeightedDutyPeriodTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedDutyPeriodTargetViews(mutableListOf<WeightedDutyPeriodTarget>(item, item)).size, "views")
    }
}
