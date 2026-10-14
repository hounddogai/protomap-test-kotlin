package com.acme.transport.terminal.sortation.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedSortationSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedSortationSnapshot("code-310", "name-437")
        val view = toWeightedSortationSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedSortationSnapshotViews(mutableListOf<WeightedSortationSnapshot>(item, item)).size, "views")
    }
}
