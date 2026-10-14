package com.acme.transport.lastmile.locker.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedLockerPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedLockerPlan("code-954", "label-870")
        val view = toBatchedLockerPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedLockerPlanViews(mutableListOf<BatchedLockerPlan>(item, item)).size, "views")
    }
}
