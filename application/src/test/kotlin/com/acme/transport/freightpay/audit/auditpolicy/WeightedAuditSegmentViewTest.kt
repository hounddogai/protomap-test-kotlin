package com.acme.transport.freightpay.audit.auditpolicy

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedAuditSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedAuditSegment("code-246", "title-581")
        val view = toWeightedAuditSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedAuditSegmentViews(mutableListOf<WeightedAuditSegment>(item, item)).size, "views")
    }
}
