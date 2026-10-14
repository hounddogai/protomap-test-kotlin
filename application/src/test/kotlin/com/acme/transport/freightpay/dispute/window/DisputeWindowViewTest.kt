package com.acme.transport.freightpay.dispute.window

import kotlin.test.Test
import kotlin.test.assertEquals

class DisputeWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDisputeWindow("code-168", "title-747")
        val view = toDisputeWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDisputeWindowViews(mutableListOf<DisputeWindow>(item, item)).size, "views")
    }
}
