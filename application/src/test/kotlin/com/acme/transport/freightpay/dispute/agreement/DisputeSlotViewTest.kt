package com.acme.transport.freightpay.dispute.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class DisputeSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDisputeSlot("code-795", "name-294")
        val view = toDisputeSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDisputeSlotViews(mutableListOf<DisputeSlot>(item, item)).size, "views")
    }
}
