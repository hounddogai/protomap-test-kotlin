package com.acme.transport.lastmile.redelivery.quota

import kotlin.test.Test
import kotlin.test.assertEquals

class RedeliveryAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRedeliveryAudit("code-309", "label-740")
        val view = toRedeliveryAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRedeliveryAuditViews(mutableListOf<RedeliveryAudit>(item, item)).size, "views")
    }
}
