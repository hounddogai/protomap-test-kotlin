package com.acme.transport.linehaul.laneplan.template

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledLanePlanSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledLanePlanSnapshot("code-265", "label-397")
        val view = toScheduledLanePlanSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledLanePlanSnapshotViews(mutableListOf<ScheduledLanePlanSnapshot>(item, item)).size, "views")
    }
}
