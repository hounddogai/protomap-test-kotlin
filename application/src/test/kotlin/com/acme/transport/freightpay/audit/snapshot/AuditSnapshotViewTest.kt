package com.acme.transport.freightpay.audit.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class AuditSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAuditSnapshot("code-656", "name-144")
        val view = toAuditSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAuditSnapshotViews(mutableListOf<AuditSnapshot>(item, item)).size, "views")
    }
}
