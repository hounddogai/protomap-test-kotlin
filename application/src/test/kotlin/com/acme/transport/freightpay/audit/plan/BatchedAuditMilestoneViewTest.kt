package com.acme.transport.freightpay.audit.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedAuditMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedAuditMilestone("code-622", "title-343")
        val view = toBatchedAuditMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedAuditMilestoneViews(mutableListOf<BatchedAuditMilestone>(item, item)).size, "views")
    }
}
