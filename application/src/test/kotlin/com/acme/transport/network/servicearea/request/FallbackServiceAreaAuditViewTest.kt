package com.acme.transport.network.servicearea.request

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackServiceAreaAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackServiceAreaAudit("code-132", "title-244")
        val view = toFallbackServiceAreaAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackServiceAreaAuditViews(mutableListOf<FallbackServiceAreaAudit>(item, item)).size, "views")
    }
}
