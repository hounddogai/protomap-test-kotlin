package com.acme.transport.crewing.dutyperiod.periodassignment

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedDutyPeriodClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedDutyPeriodClaim("code-378", "title-621")
        val view = toWeightedDutyPeriodClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedDutyPeriodClaimViews(mutableListOf<WeightedDutyPeriodClaim>(item, item)).size, "views")
    }
}
