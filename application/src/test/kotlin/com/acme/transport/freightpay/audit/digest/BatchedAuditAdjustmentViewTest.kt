package com.acme.transport.freightpay.audit.digest

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedAuditAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedAuditAdjustment("code-628", "title-985")
        val view = toBatchedAuditAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedAuditAdjustmentViews(mutableListOf<BatchedAuditAdjustment>(item, item)).size, "views")
    }
}
