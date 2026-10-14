package com.acme.transport.crewing.roster.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedRosterAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedRosterAdjustment("code-829", "label-893")
        val view = toBatchedRosterAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedRosterAdjustmentViews(mutableListOf<BatchedRosterAdjustment>(item, item)).size, "views")
    }
}
