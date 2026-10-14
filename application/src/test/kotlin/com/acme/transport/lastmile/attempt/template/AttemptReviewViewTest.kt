package com.acme.transport.lastmile.attempt.template

import kotlin.test.Test
import kotlin.test.assertEquals

class AttemptReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAttemptReview("code-831", "name-535")
        val view = toAttemptReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAttemptReviewViews(mutableListOf<AttemptReview>(item, item)).size, "views")
    }
}
