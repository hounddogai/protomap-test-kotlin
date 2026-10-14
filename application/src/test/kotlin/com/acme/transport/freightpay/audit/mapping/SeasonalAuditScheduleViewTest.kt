package com.acme.transport.freightpay.audit.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalAuditScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalAuditSchedule("code-221", "title-6")
        val view = toSeasonalAuditScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalAuditScheduleViews(mutableListOf<SeasonalAuditSchedule>(item, item)).size, "views")
    }
}
