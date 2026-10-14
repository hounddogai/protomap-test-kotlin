package com.acme.transport.terminal.traileryard.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerYardSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerYardSnapshot("code-997", "name-917")
        val view = toTrailerYardSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerYardSnapshotViews(mutableListOf<TrailerYardSnapshot>(item, item)).size, "views")
    }
}
