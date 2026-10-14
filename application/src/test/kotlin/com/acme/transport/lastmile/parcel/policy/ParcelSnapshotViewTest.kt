package com.acme.transport.lastmile.parcel.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class ParcelSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newParcelSnapshot("code-3", "label-120")
        val view = toParcelSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toParcelSnapshotViews(mutableListOf<ParcelSnapshot>(item, item)).size, "views")
    }
}
