package com.acme.transport.lastmile.attempt.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalAttemptReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalAttemptReview("code-316", "name-989")
        val view = toGlobalAttemptReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalAttemptReviewViews(mutableListOf<GlobalAttemptReview>(item, item)).size, "views")
    }
}
