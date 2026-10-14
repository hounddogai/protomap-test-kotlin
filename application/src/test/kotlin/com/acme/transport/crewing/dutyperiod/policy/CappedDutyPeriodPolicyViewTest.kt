package com.acme.transport.crewing.dutyperiod.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedDutyPeriodPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedDutyPeriodPolicy("code-352", "label-99")
        val view = toCappedDutyPeriodPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedDutyPeriodPolicyViews(mutableListOf<CappedDutyPeriodPolicy>(item, item)).size, "views")
    }
}
