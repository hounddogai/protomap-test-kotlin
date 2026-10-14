package com.acme.transport.safety.violation.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledViolationDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledViolationDraft("code-572", "name-490")
        val view = toScheduledViolationDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledViolationDraftViews(mutableListOf<ScheduledViolationDraft>(item, item)).size, "views")
    }
}
