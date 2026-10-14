package com.acme.transport.network.zone.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualZoneCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualZoneCheckpoint("code-254", "title-696")
        val view = toManualZoneCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualZoneCheckpointViews(mutableListOf<ManualZoneCheckpoint>(item, item)).size, "views")
    }
}
