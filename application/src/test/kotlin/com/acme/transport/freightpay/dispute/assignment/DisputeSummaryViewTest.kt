package com.acme.transport.freightpay.dispute.assignment

import kotlin.test.Test
import kotlin.test.assertEquals

class DisputeSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDisputeSummary("code-505", "label-992")
        val view = toDisputeSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDisputeSummaryViews(mutableListOf<DisputeSummary>(item, item)).size, "views")
    }
}
