package com.acme.transport.lastmile.drop.dropbundle

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredDropTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredDropTarget("code-281", "title-98")
        val view = toTieredDropTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredDropTargetViews(mutableListOf<TieredDropTarget>(item, item)).size, "views")
    }
}
