package com.acme.transport.terminal.crossdock.dockslot

import kotlin.test.Test
import kotlin.test.assertEquals

class CrossDockAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCrossDockAdjustment("code-226", "name-720")
        val view = toCrossDockAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCrossDockAdjustmentViews(mutableListOf<CrossDockAdjustment>(item, item)).size, "views")
    }
}
