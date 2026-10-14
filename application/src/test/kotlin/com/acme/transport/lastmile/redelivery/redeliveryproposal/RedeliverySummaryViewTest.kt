package com.acme.transport.lastmile.redelivery.redeliveryproposal

import kotlin.test.Test
import kotlin.test.assertEquals

class RedeliverySummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRedeliverySummary("code-395", "label-307")
        val view = toRedeliverySummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRedeliverySummaryViews(mutableListOf<RedeliverySummary>(item, item)).size, "views")
    }
}
