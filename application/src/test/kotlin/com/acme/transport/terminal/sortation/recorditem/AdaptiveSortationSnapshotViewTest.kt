package com.acme.transport.terminal.sortation.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveSortationSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveSortationSnapshot("code-674", "name-663")
        val view = toAdaptiveSortationSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveSortationSnapshotViews(mutableListOf<AdaptiveSortationSnapshot>(item, item)).size, "views")
    }
}
