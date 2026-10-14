package com.acme.transport.network.cutoff.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictCutoffSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictCutoffSnapshot("code-837", "name-575")
        val view = toStrictCutoffSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictCutoffSnapshotViews(mutableListOf<StrictCutoffSnapshot>(item, item)).size, "views")
    }
}
