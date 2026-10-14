package com.acme.transport.terminal.sortation.review

import kotlin.test.Test
import kotlin.test.assertEquals

class SortationReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSortationReview("code-358", "name-795")
        val view = toSortationReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSortationReviewViews(mutableListOf<SortationReview>(item, item)).size, "views")
    }
}
