package com.acme.transport.lastmile.attempt.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class AttemptRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAttemptRule("code-513", "label-407")
        val view = toAttemptRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAttemptRuleViews(mutableListOf<AttemptRule>(item, item)).size, "views")
    }
}
