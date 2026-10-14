package com.acme.transport.freightpay.audit.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveAuditManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveAuditManifest("code-544", "name-547")
        val view = toAdaptiveAuditManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveAuditManifestViews(mutableListOf<AdaptiveAuditManifest>(item, item)).size, "views")
    }
}
