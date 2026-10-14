package com.acme.transport.freightpay.dispute.disputebundle

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalDisputeBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalDisputeBatch("code-71", "title-155")
        val view = toGlobalDisputeBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalDisputeBatchViews(mutableListOf<GlobalDisputeBatch>(item, item)).size, "views")
    }
}
