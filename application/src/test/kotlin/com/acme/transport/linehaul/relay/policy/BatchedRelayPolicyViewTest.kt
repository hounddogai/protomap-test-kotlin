package com.acme.transport.linehaul.relay.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedRelayPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedRelayPolicy("code-274", "title-800")
        val view = toBatchedRelayPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedRelayPolicyViews(mutableListOf<BatchedRelayPolicy>(item, item)).size, "views")
    }
}
