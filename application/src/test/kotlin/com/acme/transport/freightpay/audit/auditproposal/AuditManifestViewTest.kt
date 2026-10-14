package com.acme.transport.freightpay.audit.auditproposal

import kotlin.test.Test
import kotlin.test.assertEquals

class AuditManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAuditManifest("code-664", "title-686")
        val view = toAuditManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAuditManifestViews(mutableListOf<AuditManifest>(item, item)).size, "views")
    }
}
