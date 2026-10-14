package com.acme.transport.network.hub.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class HubSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHubSnapshot("code-322", "title-176")
        val view = toHubSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHubSnapshotViews(mutableListOf<HubSnapshot>(item, item)).size, "views")
    }
}
