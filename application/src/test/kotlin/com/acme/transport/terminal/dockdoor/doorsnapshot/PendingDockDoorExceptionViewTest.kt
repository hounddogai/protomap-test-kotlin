package com.acme.transport.terminal.dockdoor.doorsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingDockDoorExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingDockDoorException("code-826", "title-540")
        val view = toPendingDockDoorExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingDockDoorExceptionViews(mutableListOf<PendingDockDoorException>(item, item)).size, "views")
    }
}
