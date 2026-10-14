package com.acme.transport.network.zone.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredZoneSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredZoneSnapshot("code-749", "label-348")
        val view = toTieredZoneSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredZoneSnapshotViews(mutableListOf<TieredZoneSnapshot>(item, item)).size, "views")
    }
}
