package com.acme.transport.freightpay.fuelsurcharge.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedFuelSurchargeMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedFuelSurchargeMilestone("code-648", "name-534")
        val view = toBatchedFuelSurchargeMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedFuelSurchargeMilestoneViews(mutableListOf<BatchedFuelSurchargeMilestone>(item, item)).size, "views")
    }
}
