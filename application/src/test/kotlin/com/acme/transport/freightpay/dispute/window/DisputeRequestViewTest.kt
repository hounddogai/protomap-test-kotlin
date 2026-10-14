package com.acme.transport.freightpay.dispute.window

import kotlin.test.Test
import kotlin.test.assertEquals

class DisputeRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDisputeRequest("code-396", "name-915")
        val view = toDisputeRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDisputeRequestViews(mutableListOf<DisputeRequest>(item, item)).size, "views")
    }
}
