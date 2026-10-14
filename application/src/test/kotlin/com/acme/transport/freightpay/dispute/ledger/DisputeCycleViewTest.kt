package com.acme.transport.freightpay.dispute.ledger

import kotlin.test.Test
import kotlin.test.assertEquals

class DisputeCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDisputeCycle("code-516", "label-87")
        val view = toDisputeCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDisputeCycleViews(mutableListOf<DisputeCycle>(item, item)).size, "views")
    }
}
