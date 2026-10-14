package com.acme.transport.freightpay.audit.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyAuditManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyAuditManifest("code-610", "label-649")
        val view = toLegacyAuditManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyAuditManifestViews(mutableListOf<LegacyAuditManifest>(item, item)).size, "views")
    }
}
