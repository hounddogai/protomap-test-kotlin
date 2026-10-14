package com.acme.transport.fleetops.inspection.template

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionSnapshot("code-352", "label-302")
        val view = toInspectionSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionSnapshotViews(mutableListOf<InspectionSnapshot>(item, item)).size, "views")
    }
}
