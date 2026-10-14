package com.acme.transport.crewing.dutyperiod.threshold

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingDutyPeriodPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingDutyPeriodPolicy("code-681", "title-979")
        val view = toPendingDutyPeriodPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingDutyPeriodPolicyViews(mutableListOf<PendingDutyPeriodPolicy>(item, item)).size, "views")
    }
}
