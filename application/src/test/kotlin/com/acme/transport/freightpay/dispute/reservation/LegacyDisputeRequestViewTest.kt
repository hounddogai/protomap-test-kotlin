package com.acme.transport.freightpay.dispute.reservation

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyDisputeRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyDisputeRequest("code-891", "label-548")
        val view = toLegacyDisputeRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyDisputeRequestViews(mutableListOf<LegacyDisputeRequest>(item, item)).size, "views")
    }
}
