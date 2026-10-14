package com.acme.transport.fleetops.inspection.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionCheckpoint("code-887", "title-450")
        val view = toInspectionCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionCheckpointViews(mutableListOf<InspectionCheckpoint>(item, item)).size, "views")
    }
}
