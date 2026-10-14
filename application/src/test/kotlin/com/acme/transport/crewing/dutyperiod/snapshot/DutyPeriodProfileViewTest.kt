package com.acme.transport.crewing.dutyperiod.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class DutyPeriodProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDutyPeriodProfile("code-177", "label-965")
        val view = toDutyPeriodProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDutyPeriodProfileViews(mutableListOf<DutyPeriodProfile>(item, item)).size, "views")
    }
}
