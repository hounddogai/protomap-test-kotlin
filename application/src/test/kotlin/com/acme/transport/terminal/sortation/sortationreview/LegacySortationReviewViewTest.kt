package com.acme.transport.terminal.sortation.sortationreview

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacySortationReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacySortationReview("code-552", "title-935")
        val view = toLegacySortationReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacySortationReviewViews(mutableListOf<LegacySortationReview>(item, item)).size, "views")
    }
}
