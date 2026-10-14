package com.acme.transport.crewing.dutyperiod.periodsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class DutyPeriodAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDutyPeriodAudit("code-172", "name-587")
        val view = toDutyPeriodAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDutyPeriodAuditViews(mutableListOf<DutyPeriodAudit>(item, item)).size, "views")
    }
}
