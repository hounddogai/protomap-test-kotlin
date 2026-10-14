package com.acme.transport.terminal.dockdoor.threshold

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedDockDoorWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedDockDoorWindow("code-635", "name-410")
        val view = toBatchedDockDoorWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedDockDoorWindowViews(mutableListOf<BatchedDockDoorWindow>(item, item)).size, "views")
    }
}
