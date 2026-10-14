package com.acme.transport.linehaul.capacity.request

import kotlin.test.Test
import kotlin.test.assertEquals

class CapacitySnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCapacitySnapshot("code-763", "label-777")
        val view = toCapacitySnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCapacitySnapshotViews(mutableListOf<CapacitySnapshot>(item, item)).size, "views")
    }
}
