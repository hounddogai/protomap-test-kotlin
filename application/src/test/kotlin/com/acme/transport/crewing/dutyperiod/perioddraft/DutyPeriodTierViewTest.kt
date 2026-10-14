package com.acme.transport.crewing.dutyperiod.perioddraft

import kotlin.test.Test
import kotlin.test.assertEquals

class DutyPeriodTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDutyPeriodTier("code-170", "label-368")
        val view = toDutyPeriodTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDutyPeriodTierViews(mutableListOf<DutyPeriodTier>(item, item)).size, "views")
    }
}
