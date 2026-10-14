package com.acme.transport.freightpay.audit.template

import kotlin.test.Test
import kotlin.test.assertEquals

class AuditTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAuditTemplate("code-647", "label-734")
        val view = toAuditTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAuditTemplateViews(mutableListOf<AuditTemplate>(item, item)).size, "views")
    }
}
