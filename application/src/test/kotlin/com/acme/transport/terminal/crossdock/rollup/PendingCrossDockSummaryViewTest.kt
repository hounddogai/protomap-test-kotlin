package com.acme.transport.terminal.crossdock.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingCrossDockSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingCrossDockSummary("code-449", "label-72")
        val view = toPendingCrossDockSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingCrossDockSummaryViews(mutableListOf<PendingCrossDockSummary>(item, item)).size, "views")
    }
}
