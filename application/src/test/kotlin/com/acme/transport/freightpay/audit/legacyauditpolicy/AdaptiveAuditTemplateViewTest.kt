package com.acme.transport.freightpay.audit.legacyauditpolicy

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveAuditTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveAuditTemplate("code-1", "title-974")
        val view = toAdaptiveAuditTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveAuditTemplateViews(mutableListOf<AdaptiveAuditTemplate>(item, item)).size, "views")
    }
}
