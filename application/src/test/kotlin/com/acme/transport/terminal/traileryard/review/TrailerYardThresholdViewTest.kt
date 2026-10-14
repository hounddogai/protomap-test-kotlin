package com.acme.transport.terminal.traileryard.review

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerYardThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerYardThreshold("code-702", "title-373")
        val view = toTrailerYardThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerYardThresholdViews(mutableListOf<TrailerYardThreshold>(item, item)).size, "views")
    }
}
