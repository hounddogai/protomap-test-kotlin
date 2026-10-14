package com.acme.transport.freightpay.audit.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class AuditCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAuditCheckpoint("code-462", "name-444")
        val view = toAuditCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAuditCheckpointViews(mutableListOf<AuditCheckpoint>(item, item)).size, "views")
    }
}
