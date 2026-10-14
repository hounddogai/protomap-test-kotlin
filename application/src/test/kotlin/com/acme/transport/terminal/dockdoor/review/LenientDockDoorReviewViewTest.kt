package com.acme.transport.terminal.dockdoor.review

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientDockDoorReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientDockDoorReview("code-904", "name-563")
        val view = toLenientDockDoorReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientDockDoorReviewViews(mutableListOf<LenientDockDoorReview>(item, item)).size, "views")
    }
}
