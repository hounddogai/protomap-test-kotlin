package com.acme.transport.freightpay.dispute.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalDisputeProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalDisputeProfile("code-773", "title-802")
        val view = toGlobalDisputeProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalDisputeProfileViews(mutableListOf<GlobalDisputeProfile>(item, item)).size, "views")
    }
}
