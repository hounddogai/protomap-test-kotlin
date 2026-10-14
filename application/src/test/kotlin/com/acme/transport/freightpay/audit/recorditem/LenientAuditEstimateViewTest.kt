package com.acme.transport.freightpay.audit.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientAuditEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientAuditEstimate("code-317", "label-931")
        val view = toLenientAuditEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientAuditEstimateViews(mutableListOf<LenientAuditEstimate>(item, item)).size, "views")
    }
}
