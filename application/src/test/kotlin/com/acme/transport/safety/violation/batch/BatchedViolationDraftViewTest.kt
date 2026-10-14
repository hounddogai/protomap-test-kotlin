package com.acme.transport.safety.violation.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedViolationDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedViolationDraft("code-9", "label-956")
        val view = toBatchedViolationDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedViolationDraftViews(mutableListOf<BatchedViolationDraft>(item, item)).size, "views")
    }
}
