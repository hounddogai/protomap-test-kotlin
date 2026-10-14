package com.acme.transport.freightpay.dispute.group

import kotlin.test.Test
import kotlin.test.assertEquals

class DisputeQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDisputeQuota("code-942", "title-606")
        val view = toDisputeQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDisputeQuotaViews(mutableListOf<DisputeQuota>(item, item)).size, "views")
    }
}
