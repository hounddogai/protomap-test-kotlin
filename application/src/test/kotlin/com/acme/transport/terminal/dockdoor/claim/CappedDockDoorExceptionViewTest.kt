package com.acme.transport.terminal.dockdoor.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedDockDoorExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedDockDoorException("code-23", "title-442")
        val view = toCappedDockDoorExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedDockDoorExceptionViews(mutableListOf<CappedDockDoorException>(item, item)).size, "views")
    }
}
