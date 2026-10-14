package com.acme.transport.lastmile.parcel.cappedparcelcycle

import kotlin.test.Test
import kotlin.test.assertEquals

class ParcelCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newParcelCheckpoint("code-306", "name-393")
        val view = toParcelCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toParcelCheckpointViews(mutableListOf<ParcelCheckpoint>(item, item)).size, "views")
    }
}
