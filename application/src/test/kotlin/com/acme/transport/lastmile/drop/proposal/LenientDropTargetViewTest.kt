package com.acme.transport.lastmile.drop.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientDropTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientDropTarget("code-923", "title-244")
        val view = toLenientDropTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientDropTargetViews(mutableListOf<LenientDropTarget>(item, item)).size, "views")
    }
}
