package com.acme.transport.crewing.dutyperiod.threshold

import kotlin.test.Test
import kotlin.test.assertEquals

class DutyPeriodThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDutyPeriodThreshold("code-814", "label-258")
        val view = toDutyPeriodThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDutyPeriodThresholdViews(mutableListOf<DutyPeriodThreshold>(item, item)).size, "views")
    }
}
