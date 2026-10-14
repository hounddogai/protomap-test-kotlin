package com.acme.transport.terminal.traileryard.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerYardCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerYardCheckpoint("code-272", "label-141")
        val view = toTrailerYardCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerYardCheckpointViews(mutableListOf<TrailerYardCheckpoint>(item, item)).size, "views")
    }
}
