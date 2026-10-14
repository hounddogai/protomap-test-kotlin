package com.acme.transport.terminal.traileryard.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedTrailerYardAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedTrailerYardAudit("code-428", "label-294")
        val view = toBatchedTrailerYardAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedTrailerYardAuditViews(mutableListOf<BatchedTrailerYardAudit>(item, item)).size, "views")
    }
}
