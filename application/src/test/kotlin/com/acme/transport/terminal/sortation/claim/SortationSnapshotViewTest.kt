package com.acme.transport.terminal.sortation.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class SortationSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSortationSnapshot("code-806", "name-829")
        val view = toSortationSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSortationSnapshotViews(mutableListOf<SortationSnapshot>(item, item)).size, "views")
    }
}
