package com.acme.transport.terminal.sortation.window

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingSortationBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingSortationBatch("code-337", "name-826")
        val view = toPendingSortationBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingSortationBatchViews(mutableListOf<PendingSortationBatch>(item, item)).size, "views")
    }
}
