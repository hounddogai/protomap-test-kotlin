package com.acme.transport.fleetops.tire.audit

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictTireAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictTireAudit("code-265", "label-98")
        val view = toStrictTireAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictTireAuditViews(mutableListOf<StrictTireAudit>(item, item)).size, "views")
    }
}
