package com.acme.transport.freightpay.dispute.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class DisputeProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDisputeProfile("code-679", "title-644")
        val view = toDisputeProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDisputeProfileViews(mutableListOf<DisputeProfile>(item, item)).size, "views")
    }
}
