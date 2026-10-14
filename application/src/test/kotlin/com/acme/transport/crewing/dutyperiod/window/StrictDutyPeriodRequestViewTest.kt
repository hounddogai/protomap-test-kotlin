package com.acme.transport.crewing.dutyperiod.window

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictDutyPeriodRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictDutyPeriodRequest("code-579", "name-980")
        val view = toStrictDutyPeriodRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictDutyPeriodRequestViews(mutableListOf<StrictDutyPeriodRequest>(item, item)).size, "views")
    }
}
