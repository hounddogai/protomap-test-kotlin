package com.acme.transport.freightpay.audit.bundle

import kotlin.test.Test
import kotlin.test.assertEquals

class AuditBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAuditBundle("code-845", "label-270")
        val view = toAuditBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAuditBundleViews(mutableListOf<AuditBundle>(item, item)).size, "views")
    }
}
