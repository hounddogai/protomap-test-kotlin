package com.acme.transport.linehaul.trailer.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveTrailerSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveTrailerSummary("code-976", "name-768")
        val view = toAdaptiveTrailerSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveTrailerSummaryViews(mutableListOf<AdaptiveTrailerSummary>(item, item)).size, "views")
    }
}
