package com.acme.transport.linehaul.capacity.audit

import kotlin.test.Test
import kotlin.test.assertEquals

class CapacityAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCapacityAudit("code-894", "title-903")
        val view = toCapacityAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCapacityAuditViews(mutableListOf<CapacityAudit>(item, item)).size, "views")
    }
}
