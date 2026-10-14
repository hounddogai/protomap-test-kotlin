package com.acme.transport.network.hub.rollinghubentry

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledHubSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledHubSnapshot("code-450", "label-400")
        val view = toScheduledHubSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledHubSnapshotViews(mutableListOf<ScheduledHubSnapshot>(item, item)).size, "views")
    }
}
