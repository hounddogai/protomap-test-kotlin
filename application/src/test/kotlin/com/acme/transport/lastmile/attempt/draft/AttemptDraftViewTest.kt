package com.acme.transport.lastmile.attempt.draft

import kotlin.test.Test
import kotlin.test.assertEquals

class AttemptDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAttemptDraft("code-799", "title-569")
        val view = toAttemptDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAttemptDraftViews(mutableListOf<AttemptDraft>(item, item)).size, "views")
    }
}
