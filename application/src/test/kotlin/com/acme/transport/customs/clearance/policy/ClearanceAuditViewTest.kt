package com.acme.transport.customs.clearance.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class ClearanceAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newClearanceAudit("code-370", "title-252")
        val view = toClearanceAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toClearanceAuditViews(mutableListOf<ClearanceAudit>(item, item)).size, "views")
    }
}
