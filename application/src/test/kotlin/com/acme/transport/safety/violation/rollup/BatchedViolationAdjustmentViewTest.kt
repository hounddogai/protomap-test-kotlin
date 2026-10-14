package com.acme.transport.safety.violation.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedViolationAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedViolationAdjustment("code-81", "name-128")
        val view = toBatchedViolationAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedViolationAdjustmentViews(mutableListOf<BatchedViolationAdjustment>(item, item)).size, "views")
    }
}
