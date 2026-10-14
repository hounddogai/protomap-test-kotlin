package com.acme.transport.terminal.traileryard.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictTrailerYardCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictTrailerYardCheckpoint("code-125", "title-954")
        val view = toStrictTrailerYardCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictTrailerYardCheckpointViews(mutableListOf<StrictTrailerYardCheckpoint>(item, item)).size, "views")
    }
}
