package com.acme.transport.freightpay.audit.auditbudget

import kotlin.test.Test
import kotlin.test.assertEquals

class AuditBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAuditBatch("code-703", "label-825")
        val view = toAuditBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAuditBatchViews(mutableListOf<AuditBatch>(item, item)).size, "views")
    }
}
