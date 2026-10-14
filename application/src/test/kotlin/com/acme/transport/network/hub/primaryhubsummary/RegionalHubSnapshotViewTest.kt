package com.acme.transport.network.hub.primaryhubsummary

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalHubSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalHubSnapshot("code-878", "name-586")
        val view = toRegionalHubSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalHubSnapshotViews(mutableListOf<RegionalHubSnapshot>(item, item)).size, "views")
    }
}
