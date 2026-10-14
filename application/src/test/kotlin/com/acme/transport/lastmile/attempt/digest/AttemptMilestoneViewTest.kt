package com.acme.transport.lastmile.attempt.digest

import kotlin.test.Test
import kotlin.test.assertEquals

class AttemptMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAttemptMilestone("code-950", "title-223")
        val view = toAttemptMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAttemptMilestoneViews(mutableListOf<AttemptMilestone>(item, item)).size, "views")
    }
}
