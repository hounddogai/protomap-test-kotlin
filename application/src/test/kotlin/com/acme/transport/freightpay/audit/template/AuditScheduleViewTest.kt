package com.acme.transport.freightpay.audit.template

import kotlin.test.Test
import kotlin.test.assertEquals

class AuditScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAuditSchedule("code-396", "name-586")
        val view = toAuditScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAuditScheduleViews(mutableListOf<AuditSchedule>(item, item)).size, "views")
    }
}
