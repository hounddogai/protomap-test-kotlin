package com.acme.transport.crewing.dutyperiod.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class DutyPeriodBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDutyPeriodBatch("code-60", "title-275")
        val view = toDutyPeriodBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDutyPeriodBatchViews(mutableListOf<DutyPeriodBatch>(item, item)).size, "views")
    }
}
