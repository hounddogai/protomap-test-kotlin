package com.acme.transport.fleetops.inspection.inspectiongroup

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingInspectionCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingInspectionCheckpoint("code-656", "name-299")
        val view = toRollingInspectionCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingInspectionCheckpointViews(mutableListOf<RollingInspectionCheckpoint>(item, item)).size, "views")
    }
}
