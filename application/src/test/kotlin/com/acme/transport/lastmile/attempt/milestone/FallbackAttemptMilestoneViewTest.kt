package com.acme.transport.lastmile.attempt.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackAttemptMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackAttemptMilestone("code-535", "label-870")
        val view = toFallbackAttemptMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackAttemptMilestoneViews(mutableListOf<FallbackAttemptMilestone>(item, item)).size, "views")
    }
}
