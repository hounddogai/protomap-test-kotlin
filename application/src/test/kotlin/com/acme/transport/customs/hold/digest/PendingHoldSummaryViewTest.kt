package com.acme.transport.customs.hold.digest

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingHoldSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingHoldSummary("code-681", "label-4")
        val view = toPendingHoldSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingHoldSummaryViews(mutableListOf<PendingHoldSummary>(item, item)).size, "views")
    }
}
