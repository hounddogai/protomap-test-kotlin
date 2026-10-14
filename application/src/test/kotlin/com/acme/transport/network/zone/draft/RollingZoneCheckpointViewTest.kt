package com.acme.transport.network.zone.draft

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingZoneCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingZoneCheckpoint("code-870", "title-634")
        val view = toRollingZoneCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingZoneCheckpointViews(mutableListOf<RollingZoneCheckpoint>(item, item)).size, "views")
    }
}
