package com.acme.transport.terminal.traileryard.audit

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalTrailerYardAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalTrailerYardAudit("code-567", "name-173")
        val view = toGlobalTrailerYardAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalTrailerYardAuditViews(mutableListOf<GlobalTrailerYardAudit>(item, item)).size, "views")
    }
}
