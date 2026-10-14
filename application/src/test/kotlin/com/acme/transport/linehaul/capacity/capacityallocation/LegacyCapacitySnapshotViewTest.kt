package com.acme.transport.linehaul.capacity.capacityallocation

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyCapacitySnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyCapacitySnapshot("code-893", "name-36")
        val view = toLegacyCapacitySnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyCapacitySnapshotViews(mutableListOf<LegacyCapacitySnapshot>(item, item)).size, "views")
    }
}
