package com.acme.transport.terminal.traileryard.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerYardSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerYardSummary("code-578", "name-481")
        val view = toTrailerYardSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerYardSummaryViews(mutableListOf<TrailerYardSummary>(item, item)).size, "views")
    }
}
