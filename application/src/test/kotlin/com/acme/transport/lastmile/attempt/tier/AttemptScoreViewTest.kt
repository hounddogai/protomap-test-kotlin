package com.acme.transport.lastmile.attempt.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class AttemptScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAttemptScore("code-924", "name-359")
        val view = toAttemptScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAttemptScoreViews(mutableListOf<AttemptScore>(item, item)).size, "views")
    }
}
