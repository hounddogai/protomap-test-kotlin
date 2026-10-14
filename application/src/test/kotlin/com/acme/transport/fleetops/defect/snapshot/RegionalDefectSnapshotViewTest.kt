package com.acme.transport.fleetops.defect.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalDefectSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalDefectSnapshot("code-480", "label-195")
        val view = toRegionalDefectSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalDefectSnapshotViews(mutableListOf<RegionalDefectSnapshot>(item, item)).size, "views")
    }
}
