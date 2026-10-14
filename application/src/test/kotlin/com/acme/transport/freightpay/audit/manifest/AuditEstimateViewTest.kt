package com.acme.transport.freightpay.audit.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class AuditEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAuditEstimate("code-221", "title-639")
        val view = toAuditEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAuditEstimateViews(mutableListOf<AuditEstimate>(item, item)).size, "views")
    }
}
