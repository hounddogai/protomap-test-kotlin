package com.acme.transport.freightpay.audit.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class AuditMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAuditMetric("code-82", "title-601")
        val view = toAuditMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAuditMetricViews(mutableListOf<AuditMetric>(item, item)).size, "views")
    }
}
