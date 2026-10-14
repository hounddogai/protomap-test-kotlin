package com.acme.transport.crewing.dutyperiod.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualDutyPeriodSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualDutyPeriodSlot("code-76", "label-538")
        val view = toManualDutyPeriodSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualDutyPeriodSlotViews(mutableListOf<ManualDutyPeriodSlot>(item, item)).size, "views")
    }
}
