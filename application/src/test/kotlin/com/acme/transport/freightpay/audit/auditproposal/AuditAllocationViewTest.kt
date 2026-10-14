package com.acme.transport.freightpay.audit.auditproposal

import kotlin.test.Test
import kotlin.test.assertEquals

class AuditAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAuditAllocation("code-394", "title-115")
        val view = toAuditAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAuditAllocationViews(mutableListOf<AuditAllocation>(item, item)).size, "views")
    }
}
