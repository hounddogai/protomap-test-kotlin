package com.acme.transport.freightpay.dispute.group

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictDisputeSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictDisputeSummary("code-646", "name-976")
        val view = toStrictDisputeSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictDisputeSummaryViews(mutableListOf<StrictDisputeSummary>(item, item)).size, "views")
    }
}
