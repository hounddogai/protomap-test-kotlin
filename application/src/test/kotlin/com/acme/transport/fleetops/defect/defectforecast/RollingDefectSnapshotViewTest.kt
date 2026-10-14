package com.acme.transport.fleetops.defect.defectforecast

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingDefectSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingDefectSnapshot("code-831", "label-291")
        val view = toRollingDefectSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingDefectSnapshotViews(mutableListOf<RollingDefectSnapshot>(item, item)).size, "views")
    }
}
