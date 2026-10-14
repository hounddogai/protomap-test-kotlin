package com.acme.transport.crewing.dutyperiod.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualDutyPeriodThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualDutyPeriodThreshold("code-725", "label-246")
        val view = toManualDutyPeriodThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualDutyPeriodThresholdViews(mutableListOf<ManualDutyPeriodThreshold>(item, item)).size, "views")
    }
}
