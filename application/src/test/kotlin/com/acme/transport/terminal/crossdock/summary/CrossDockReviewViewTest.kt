package com.acme.transport.terminal.crossdock.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class CrossDockReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCrossDockReview("code-157", "title-799")
        val view = toCrossDockReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCrossDockReviewViews(mutableListOf<CrossDockReview>(item, item)).size, "views")
    }
}
