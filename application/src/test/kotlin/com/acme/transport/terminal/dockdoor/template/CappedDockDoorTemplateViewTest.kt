package com.acme.transport.terminal.dockdoor.template

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedDockDoorTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedDockDoorTemplate("code-499", "label-16")
        val view = toCappedDockDoorTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedDockDoorTemplateViews(mutableListOf<CappedDockDoorTemplate>(item, item)).size, "views")
    }
}
