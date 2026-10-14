package com.acme.transport.terminal.dockdoor.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictDockDoorBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictDockDoorBatch("code-385", "title-194")
        val view = toStrictDockDoorBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictDockDoorBatchViews(mutableListOf<StrictDockDoorBatch>(item, item)).size, "views")
    }
}
