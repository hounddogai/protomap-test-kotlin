package com.acme.transport.fleetops.defect.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class DefectSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefectSnapshot("code-364", "name-752")
        val view = toDefectSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefectSnapshotViews(mutableListOf<DefectSnapshot>(item, item)).size, "views")
    }
}
