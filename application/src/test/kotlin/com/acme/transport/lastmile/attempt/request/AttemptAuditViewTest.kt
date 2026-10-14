package com.acme.transport.lastmile.attempt.request

import kotlin.test.Test
import kotlin.test.assertEquals

class AttemptAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAttemptAudit("code-884", "title-194")
        val view = toAttemptAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAttemptAuditViews(mutableListOf<AttemptAudit>(item, item)).size, "views")
    }
}
