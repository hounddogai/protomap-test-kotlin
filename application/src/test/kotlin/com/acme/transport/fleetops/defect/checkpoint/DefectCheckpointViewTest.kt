package com.acme.transport.fleetops.defect.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals

class DefectCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefectCheckpoint("code-432", "name-748")
        val view = toDefectCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefectCheckpointViews(mutableListOf<DefectCheckpoint>(item, item)).size, "views")
    }
}
