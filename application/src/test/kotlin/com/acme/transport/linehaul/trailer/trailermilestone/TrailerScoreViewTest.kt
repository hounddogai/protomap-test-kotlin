package com.acme.transport.linehaul.trailer.trailermilestone

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerScore("code-983", "label-26")
        val view = toTrailerScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerScoreViews(mutableListOf<TrailerScore>(item, item)).size, "views")
    }
}
