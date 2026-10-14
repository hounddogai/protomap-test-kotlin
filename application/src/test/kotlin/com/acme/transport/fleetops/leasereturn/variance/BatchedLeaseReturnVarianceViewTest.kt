package com.acme.transport.fleetops.leasereturn.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedLeaseReturnVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedLeaseReturnVariance("code-707", "label-704")
        val view = toBatchedLeaseReturnVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedLeaseReturnVarianceViews(mutableListOf<BatchedLeaseReturnVariance>(item, item)).size, "views")
    }
}
