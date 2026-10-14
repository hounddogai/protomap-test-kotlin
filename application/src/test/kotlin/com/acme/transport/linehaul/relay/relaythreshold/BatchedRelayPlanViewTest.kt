package com.acme.transport.linehaul.relay.relaythreshold

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedRelayPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedRelayPlan("code-403", "label-696")
        val view = toBatchedRelayPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedRelayPlanViews(mutableListOf<BatchedRelayPlan>(item, item)).size, "views")
    }
}
