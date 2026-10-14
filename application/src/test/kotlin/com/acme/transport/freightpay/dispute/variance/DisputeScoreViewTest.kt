package com.acme.transport.freightpay.dispute.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class DisputeScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDisputeScore("code-373", "title-284")
        val view = toDisputeScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDisputeScoreViews(mutableListOf<DisputeScore>(item, item)).size, "views")
    }
}
