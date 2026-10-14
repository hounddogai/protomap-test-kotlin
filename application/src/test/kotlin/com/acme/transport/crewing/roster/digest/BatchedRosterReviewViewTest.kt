package com.acme.transport.crewing.roster.digest

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedRosterReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedRosterReview("code-66", "name-807")
        val view = toBatchedRosterReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedRosterReviewViews(mutableListOf<BatchedRosterReview>(item, item)).size, "views")
    }
}
