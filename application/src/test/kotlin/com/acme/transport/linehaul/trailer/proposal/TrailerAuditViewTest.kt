package com.acme.transport.linehaul.trailer.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerAudit("code-926", "name-764")
        val view = toTrailerAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerAuditViews(mutableListOf<TrailerAudit>(item, item)).size, "views")
    }
}
