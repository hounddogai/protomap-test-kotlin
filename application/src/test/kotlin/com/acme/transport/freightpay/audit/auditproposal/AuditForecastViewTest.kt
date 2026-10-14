package com.acme.transport.freightpay.audit.auditproposal

import kotlin.test.Test
import kotlin.test.assertEquals

class AuditForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAuditForecast("code-307", "label-238")
        val view = toAuditForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAuditForecastViews(mutableListOf<AuditForecast>(item, item)).size, "views")
    }
}
