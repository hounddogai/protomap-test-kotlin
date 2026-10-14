package com.acme.transport.freightpay.dispute.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientDisputeAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientDisputeAudit("code-568", "title-609")
        val view = toLenientDisputeAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientDisputeAuditViews(mutableListOf<LenientDisputeAudit>(item, item)).size, "views")
    }
}
