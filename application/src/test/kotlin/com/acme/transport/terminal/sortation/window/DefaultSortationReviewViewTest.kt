package com.acme.transport.terminal.sortation.window

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultSortationReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultSortationReview("code-217", "label-588")
        val view = toDefaultSortationReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultSortationReviewViews(mutableListOf<DefaultSortationReview>(item, item)).size, "views")
    }
}
