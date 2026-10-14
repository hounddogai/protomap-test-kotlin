package com.acme.transport.emissions.idling.digest

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledIdlingAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledIdlingAudit("code-177", "label-702")
        val view = toScheduledIdlingAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledIdlingAuditViews(mutableListOf<ScheduledIdlingAudit>(item, item)).size, "views")
    }
}
