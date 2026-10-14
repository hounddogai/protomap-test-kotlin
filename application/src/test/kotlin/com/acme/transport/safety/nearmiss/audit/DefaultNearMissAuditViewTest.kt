package com.acme.transport.safety.nearmiss.audit

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultNearMissAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultNearMissAudit("code-850", "title-810")
        val view = toDefaultNearMissAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultNearMissAuditViews(mutableListOf<DefaultNearMissAudit>(item, item)).size, "views")
    }
}
