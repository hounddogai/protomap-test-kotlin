package com.acme.transport.fleetops.tire.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedTireSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedTireSnapshot("code-720", "name-812")
        val view = toCappedTireSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedTireSnapshotViews(mutableListOf<CappedTireSnapshot>(item, item)).size, "views")
    }
}
