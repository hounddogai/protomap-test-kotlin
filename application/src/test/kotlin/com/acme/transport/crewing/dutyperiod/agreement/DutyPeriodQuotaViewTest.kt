package com.acme.transport.crewing.dutyperiod.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class DutyPeriodQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDutyPeriodQuota("code-542", "title-460")
        val view = toDutyPeriodQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDutyPeriodQuotaViews(mutableListOf<DutyPeriodQuota>(item, item)).size, "views")
    }
}
