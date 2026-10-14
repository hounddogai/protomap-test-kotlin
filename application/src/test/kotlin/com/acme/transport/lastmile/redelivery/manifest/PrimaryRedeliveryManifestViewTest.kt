package com.acme.transport.lastmile.redelivery.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryRedeliveryManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryRedeliveryManifest("code-721", "title-689")
        val view = toPrimaryRedeliveryManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryRedeliveryManifestViews(mutableListOf<PrimaryRedeliveryManifest>(item, item)).size, "views")
    }
}
