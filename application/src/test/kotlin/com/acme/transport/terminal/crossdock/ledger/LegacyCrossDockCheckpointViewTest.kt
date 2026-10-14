package com.acme.transport.terminal.crossdock.ledger

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyCrossDockCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyCrossDockCheckpoint("code-814", "label-432")
        val view = toLegacyCrossDockCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyCrossDockCheckpointViews(mutableListOf<LegacyCrossDockCheckpoint>(item, item)).size, "views")
    }
}
