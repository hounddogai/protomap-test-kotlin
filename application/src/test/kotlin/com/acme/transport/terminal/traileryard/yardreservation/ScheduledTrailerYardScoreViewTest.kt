package com.acme.transport.terminal.traileryard.yardreservation

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledTrailerYardScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledTrailerYardScore("code-999", "name-759")
        val view = toScheduledTrailerYardScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledTrailerYardScoreViews(mutableListOf<ScheduledTrailerYardScore>(item, item)).size, "views")
    }
}
