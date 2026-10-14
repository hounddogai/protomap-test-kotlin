package com.acme.transport.terminal.traileryard.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerYardBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerYardBatch("code-564", "name-548")
        val view = toTrailerYardBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerYardBatchViews(mutableListOf<TrailerYardBatch>(item, item)).size, "views")
    }
}
