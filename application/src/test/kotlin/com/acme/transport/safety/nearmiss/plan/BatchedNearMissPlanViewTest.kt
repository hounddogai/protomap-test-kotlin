package com.acme.transport.safety.nearmiss.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedNearMissPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedNearMissPlan("code-885", "name-179")
        val view = toBatchedNearMissPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedNearMissPlanViews(mutableListOf<BatchedNearMissPlan>(item, item)).size, "views")
    }
}
