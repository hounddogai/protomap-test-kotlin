package com.acme.transport.freightpay.dispute.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class DisputeAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDisputeAudit("code-460", "name-397")
        val view = toDisputeAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDisputeAuditViews(mutableListOf<DisputeAudit>(item, item)).size, "views")
    }
}
