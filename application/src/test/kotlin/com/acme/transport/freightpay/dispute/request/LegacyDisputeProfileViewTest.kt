package com.acme.transport.freightpay.dispute.request

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyDisputeProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyDisputeProfile("code-643", "label-244")
        val view = toLegacyDisputeProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyDisputeProfileViews(mutableListOf<LegacyDisputeProfile>(item, item)).size, "views")
    }
}
