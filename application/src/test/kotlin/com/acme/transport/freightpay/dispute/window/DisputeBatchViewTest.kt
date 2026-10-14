package com.acme.transport.freightpay.dispute.window

import kotlin.test.Test
import kotlin.test.assertEquals

class DisputeBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDisputeBatch("code-719", "title-641")
        val view = toDisputeBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDisputeBatchViews(mutableListOf<DisputeBatch>(item, item)).size, "views")
    }
}
