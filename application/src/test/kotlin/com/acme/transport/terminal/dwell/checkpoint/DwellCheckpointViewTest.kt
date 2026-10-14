package com.acme.transport.terminal.dwell.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals

class DwellCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDwellCheckpoint("code-515", "label-803")
        val view = toDwellCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDwellCheckpointViews(mutableListOf<DwellCheckpoint>(item, item)).size, "views")
    }
}
