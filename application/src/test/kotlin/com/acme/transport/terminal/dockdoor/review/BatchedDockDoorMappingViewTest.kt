package com.acme.transport.terminal.dockdoor.review

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedDockDoorMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedDockDoorMapping("code-846", "title-558")
        val view = toBatchedDockDoorMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedDockDoorMappingViews(mutableListOf<BatchedDockDoorMapping>(item, item)).size, "views")
    }
}
