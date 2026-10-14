package com.acme.transport.terminal.sortation.review

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingSortationReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingSortationReview("code-905", "label-819")
        val view = toRollingSortationReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingSortationReviewViews(mutableListOf<RollingSortationReview>(item, item)).size, "views")
    }
}
