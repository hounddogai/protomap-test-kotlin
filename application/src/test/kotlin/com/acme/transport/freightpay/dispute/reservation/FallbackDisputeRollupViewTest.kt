package com.acme.transport.freightpay.dispute.reservation

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackDisputeRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackDisputeRollup("code-590", "title-762")
        val view = toFallbackDisputeRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackDisputeRollupViews(mutableListOf<FallbackDisputeRollup>(item, item)).size, "views")
    }
}
