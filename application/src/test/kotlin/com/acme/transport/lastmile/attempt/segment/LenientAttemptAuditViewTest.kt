package com.acme.transport.lastmile.attempt.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientAttemptAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientAttemptAudit("code-256", "name-257")
        val view = toLenientAttemptAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientAttemptAuditViews(mutableListOf<LenientAttemptAudit>(item, item)).size, "views")
    }
}
