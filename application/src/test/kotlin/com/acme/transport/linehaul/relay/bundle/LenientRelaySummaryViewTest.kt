package com.acme.transport.linehaul.relay.bundle

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientRelaySummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientRelaySummary("code-110", "title-526")
        val view = toLenientRelaySummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientRelaySummaryViews(mutableListOf<LenientRelaySummary>(item, item)).size, "views")
    }
}
