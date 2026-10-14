package com.acme.transport.terminal.crossdock.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyCrossDockSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyCrossDockSummary("code-883", "title-264")
        val view = toLegacyCrossDockSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyCrossDockSummaryViews(mutableListOf<LegacyCrossDockSummary>(item, item)).size, "views")
    }
}
