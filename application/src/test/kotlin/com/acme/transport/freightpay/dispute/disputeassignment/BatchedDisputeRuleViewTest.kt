package com.acme.transport.freightpay.dispute.disputeassignment

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedDisputeRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedDisputeRule("code-853", "name-239")
        val view = toBatchedDisputeRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedDisputeRuleViews(mutableListOf<BatchedDisputeRule>(item, item)).size, "views")
    }
}
