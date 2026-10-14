package com.acme.transport.lastmile.attempt.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryAttemptScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryAttemptScore("code-427", "name-315")
        val view = toPrimaryAttemptScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryAttemptScoreViews(mutableListOf<PrimaryAttemptScore>(item, item)).size, "views")
    }
}
