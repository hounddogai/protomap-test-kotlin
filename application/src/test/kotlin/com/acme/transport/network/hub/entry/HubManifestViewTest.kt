package com.acme.transport.network.hub.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class HubManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHubManifest("code-270", "label-333")
        val view = toHubManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHubManifestViews(mutableListOf<HubManifest>(item, item)).size, "views")
    }
}
