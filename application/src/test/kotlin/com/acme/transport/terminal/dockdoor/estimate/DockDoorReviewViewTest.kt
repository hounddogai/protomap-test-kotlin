package com.acme.transport.terminal.dockdoor.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class DockDoorReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDockDoorReview("code-869", "name-879")
        val view = toDockDoorReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDockDoorReviewViews(mutableListOf<DockDoorReview>(item, item)).size, "views")
    }
}
