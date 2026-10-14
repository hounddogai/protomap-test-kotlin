package com.acme.transport.safety.violation.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class ViolationDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newViolationDraft("code-70", "label-966")
        val view = toViolationDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toViolationDraftViews(mutableListOf<ViolationDraft>(item, item)).size, "views")
    }
}
