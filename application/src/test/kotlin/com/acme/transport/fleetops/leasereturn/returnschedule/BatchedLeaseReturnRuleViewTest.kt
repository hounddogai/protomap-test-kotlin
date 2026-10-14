package com.acme.transport.fleetops.leasereturn.returnschedule

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedLeaseReturnRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedLeaseReturnRule("code-249", "title-169")
        val view = toBatchedLeaseReturnRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedLeaseReturnRuleViews(mutableListOf<BatchedLeaseReturnRule>(item, item)).size, "views")
    }
}
