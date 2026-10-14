package com.acme.transport.freightpay.dispute.disputemanifest

import kotlin.test.Test
import kotlin.test.assertEquals

class DisputeMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDisputeMapping("code-454", "name-425")
        val view = toDisputeMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDisputeMappingViews(mutableListOf<DisputeMapping>(item, item)).size, "views")
    }
}
