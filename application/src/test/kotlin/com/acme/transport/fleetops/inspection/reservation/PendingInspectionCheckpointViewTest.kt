package com.acme.transport.fleetops.inspection.reservation

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingInspectionCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingInspectionCheckpoint("code-363", "title-710")
        val view = toPendingInspectionCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingInspectionCheckpointViews(mutableListOf<PendingInspectionCheckpoint>(item, item)).size, "views")
    }
}
