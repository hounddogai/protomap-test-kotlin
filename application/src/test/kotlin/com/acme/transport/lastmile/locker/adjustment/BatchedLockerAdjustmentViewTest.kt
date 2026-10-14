package com.acme.transport.lastmile.locker.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedLockerAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedLockerAdjustment("code-288", "name-143")
        val view = toBatchedLockerAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedLockerAdjustmentViews(mutableListOf<BatchedLockerAdjustment>(item, item)).size, "views")
    }
}
