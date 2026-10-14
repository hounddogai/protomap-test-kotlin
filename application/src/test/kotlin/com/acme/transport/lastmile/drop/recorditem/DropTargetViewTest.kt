package com.acme.transport.lastmile.drop.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class DropTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDropTarget("code-209", "title-886")
        val view = toDropTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDropTargetViews(mutableListOf<DropTarget>(item, item)).size, "views")
    }
}
