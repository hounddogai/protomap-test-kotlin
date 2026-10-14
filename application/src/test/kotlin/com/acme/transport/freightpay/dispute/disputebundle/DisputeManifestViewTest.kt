package com.acme.transport.freightpay.dispute.disputebundle

import kotlin.test.Test
import kotlin.test.assertEquals

class DisputeManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDisputeManifest("code-421", "title-861")
        val view = toDisputeManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDisputeManifestViews(mutableListOf<DisputeManifest>(item, item)).size, "views")
    }
}
