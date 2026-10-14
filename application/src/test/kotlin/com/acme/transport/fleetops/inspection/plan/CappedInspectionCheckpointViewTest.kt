package com.acme.transport.fleetops.inspection.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedInspectionCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedInspectionCheckpoint("code-490", "name-391")
        val view = toCappedInspectionCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedInspectionCheckpointViews(mutableListOf<CappedInspectionCheckpoint>(item, item)).size, "views")
    }
}
