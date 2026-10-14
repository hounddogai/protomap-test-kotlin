package com.acme.transport.linehaul.capacity.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveCapacitySnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveCapacitySnapshot("code-71", "label-125")
        val view = toAdaptiveCapacitySnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveCapacitySnapshotViews(mutableListOf<AdaptiveCapacitySnapshot>(item, item)).size, "views")
    }
}
