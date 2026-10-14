package com.acme.transport.terminal.crossdock.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class CrossDockSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCrossDockSummary("code-686", "label-499")
        val view = toCrossDockSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCrossDockSummaryViews(mutableListOf<CrossDockSummary>(item, item)).size, "views")
    }
}
