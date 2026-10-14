package com.acme.transport.freightpay.audit.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredAuditManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredAuditManifest("code-247", "title-244")
        val view = toTieredAuditManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredAuditManifestViews(mutableListOf<TieredAuditManifest>(item, item)).size, "views")
    }
}
