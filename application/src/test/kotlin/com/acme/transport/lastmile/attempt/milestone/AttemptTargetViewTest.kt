package com.acme.transport.lastmile.attempt.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class AttemptTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAttemptTarget("code-658", "title-195")
        val view = toAttemptTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAttemptTargetViews(mutableListOf<AttemptTarget>(item, item)).size, "views")
    }
}
