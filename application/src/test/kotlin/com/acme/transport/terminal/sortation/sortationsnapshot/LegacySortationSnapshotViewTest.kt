package com.acme.transport.terminal.sortation.sortationsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacySortationSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacySortationSnapshot("code-972", "label-703")
        val view = toLegacySortationSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacySortationSnapshotViews(mutableListOf<LegacySortationSnapshot>(item, item)).size, "views")
    }
}
