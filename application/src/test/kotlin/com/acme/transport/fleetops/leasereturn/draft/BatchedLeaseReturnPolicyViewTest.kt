package com.acme.transport.fleetops.leasereturn.draft

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedLeaseReturnPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedLeaseReturnPolicy("code-440", "label-691")
        val view = toBatchedLeaseReturnPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedLeaseReturnPolicyViews(mutableListOf<BatchedLeaseReturnPolicy>(item, item)).size, "views")
    }
}
