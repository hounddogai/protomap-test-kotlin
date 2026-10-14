package com.acme.transport.linehaul.capacity.capacitysnapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryCapacitySnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryCapacitySnapshot("code-742", "name-471")
        val view = toPrimaryCapacitySnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryCapacitySnapshotViews(mutableListOf<PrimaryCapacitySnapshot>(item, item)).size, "views")
    }
}
